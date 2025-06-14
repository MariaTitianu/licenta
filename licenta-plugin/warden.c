#include "/usr/include/postgresql/16/server/postgres.h"

#include <math.h>
#include <sys/stat.h>
#include <unistd.h>
#include <setjmp.h>
#include <time.h>

#include "access/hash.h"
#include "access/heapam.h"
#include "catalog/pg_authid.h"
#include "catalog/namespace.h"
#include "catalog/pg_type.h"
#include "executor/executor.h"
#include "executor/instrument.h"
#include "funcapi.h"
#include "mb/pg_wchar.h"
#include "miscadmin.h"
#include "nodes/nodes.h"
#include "nodes/parsenodes.h"
#include "parser/analyze.h"
#include "parser/parsetree.h"
#include "parser/parser.h"
#include "parser/scanner.h"
#include "parser/scansup.h"
#include "pgstat.h"
#include "storage/fd.h"
#include "storage/ipc.h"
#include "storage/spin.h"
#include "tcop/utility.h"
#include "utils/acl.h"
#include "utils/builtins.h"
#include "utils/fmgroids.h"
#include "utils/lsyscache.h"
#include "utils/memutils.h"
#include "utils/rel.h"
#include "utils/relcache.h"
#include "utils/syscache.h"
#include "commands/tablecmds.h"
#include "tcop/tcopprot.h"
#include "parser/analyze.h"
#include "utils/timestamp.h"
#include "utils/datetime.h"

PG_MODULE_MAGIC;

void _PG_init(void);
void _PG_fini(void);


static int nested_level = 0;
static ProcessUtility_hook_type prev_ProcessUtility = NULL;
static post_parse_analyze_hook_type prev_post_parse_analyze_hook = NULL;


static void log_operation_csv(const char *operation_type, const char *table_name, 
                              const char *status, const char *blocked_reason, 
                              const char *query_text);
static char *escape_csv_field(const char *str);
static bool is_unprotected_table(const char *table_name);
static void intercept_dml_command(ParseState *pstate, Query *query);
static bool is_protected_ddl_command(Node *parsetree, char **table_name);


Datum warden_all_queries(PG_FUNCTION_ARGS);
Datum warden_protect(PG_FUNCTION_ARGS);
Datum warden_unprotect(PG_FUNCTION_ARGS);

PG_FUNCTION_INFO_V1(warden_all_queries);
PG_FUNCTION_INFO_V1(warden_protect);
PG_FUNCTION_INFO_V1(warden_unprotect);

static void process_utility(PlannedStmt *pstmt,
                            const char *queryString,
                            bool readOnlyTree,
                            ProcessUtilityContext context,
                            ParamListInfo params,
                            QueryEnvironment *queryEnv,
                            DestReceiver *dest,
                            QueryCompletion *qc);


static void warden_post_parse_analyze(ParseState *pstate, Query *query, JumbleState *jstate);

void
_PG_init(void)
{
    elog(NOTICE, "pg_warden extension initializing");
    
    
    prev_ProcessUtility = ProcessUtility_hook;
    ProcessUtility_hook = process_utility;
    
    prev_post_parse_analyze_hook = post_parse_analyze_hook;
    post_parse_analyze_hook = warden_post_parse_analyze;
    
    elog(NOTICE, "pg_warden hooks installed successfully");
}

void
_PG_fini(void)
{
    elog(NOTICE, "pg_warden extension unloading");
    ProcessUtility_hook = prev_ProcessUtility;
    post_parse_analyze_hook = prev_post_parse_analyze_hook;
}

/* Removed write_file function - now using log_operation_csv */

static char *
escape_csv_field(const char *str)
{
    bool needs_quotes = false;
    const char *p;
    int quote_count = 0;
    char *result;
    char *r;
    
    if (str == NULL)
        return pstrdup("");
    
    /* Check if escaping is needed */
    for (p = str; *p; p++)
    {
        if (*p == '"' || *p == ',' || *p == '\n' || *p == '\r')
        {
            needs_quotes = true;
            break;
        }
    }
    
    if (!needs_quotes)
        return pstrdup(str);
    
    /* Count quotes to determine buffer size */
    for (p = str; *p; p++)
    {
        if (*p == '"')
            quote_count++;
    }
    
    /* Allocate buffer: original length + quotes + escaped quotes + null terminator */
    result = palloc(strlen(str) + quote_count + 3);
    r = result;
    
    *r++ = '"';
    for (p = str; *p; p++)
    {
        if (*p == '"')
        {
            *r++ = '"';
            *r++ = '"';
        }
        else
        {
            *r++ = *p;
        }
    }
    *r++ = '"';
    *r = '\0';
    
    return result;
}

static void
log_operation_csv(const char *operation_type, const char *table_name, 
                  const char *status, const char *blocked_reason, 
                  const char *query_text)
{
    char log_path[MAXPGPATH];
    FILE *fp;
    char timestamp_str[128];
    char *escaped_query;
    char *escaped_reason;
    char *user_name;
    time_t now;
    struct tm *tm_info;
    
    /* Build log file path in data directory */
    snprintf(log_path, sizeof(log_path), "%s/pg_warden_ops.csv", DataDir);
    
    /* Open file for appending */
    fp = fopen(log_path, "a+");
    if (fp == NULL)
    {
        elog(WARNING, "pg_warden: unable to open log file %s", log_path);
        return;
    }
    
    /* Get current timestamp in ISO format */
    now = time(NULL);
    tm_info = gmtime(&now);
    strftime(timestamp_str, sizeof(timestamp_str), "%Y-%m-%dT%H:%M:%SZ", tm_info);
    
    /* Get user name */
    user_name = GetUserNameFromId(GetUserId(), false);
    
    /* Escape CSV fields */
    escaped_query = escape_csv_field(query_text);
    escaped_reason = escape_csv_field(blocked_reason);
    
    /* Write CSV row (removed database_name field) */
    fprintf(fp, "%s,%s,%s,%s,%d,%s,%s,%s\n",
            timestamp_str,
            operation_type ? operation_type : "",
            table_name ? table_name : "",
            user_name ? user_name : "",
            MyProcPid,
            status ? status : "",
            escaped_reason,
            escaped_query);
    
    fclose(fp);
    
    /* Clean up */
    if (escaped_query)
        pfree(escaped_query);
    if (escaped_reason)
        pfree(escaped_reason);
}


/* Removed is_alter_table_command - no longer needed */


static bool
is_unprotected_table(const char *table_name)
{
    bool result = false;
    Relation rel;
    SysScanDesc scan;
    HeapTuple tuple;
    ScanKeyData key[1];
    
    elog(NOTICE, "checking if table '%s' is unprotected", table_name);
    
   
    if (strcmp(table_name, "warden_unprotected_tables") == 0)
    {
        elog(NOTICE, "warden_unprotected_tables is always unprotected");
        return true;
    }
    
  
    rel = table_open(get_relname_relid("warden_unprotected_tables", 
                                  get_namespace_oid("public", false)), 
                AccessShareLock);
    if (rel == NULL)
    {
        elog(WARNING, "Failed to open warden_unprotected_tables");
        return false;
    }
    
    
    ScanKeyInit(&key[0],
                1, 
                BTEqualStrategyNumber,
                F_TEXTEQ,
                CStringGetTextDatum(table_name));

    
    scan = systable_beginscan(rel, InvalidOid, false, NULL, 1, key);
    
    tuple = systable_getnext(scan);
    if (HeapTupleIsValid(tuple))
    {
        result = true;
        elog(NOTICE, "Table '%s' is unprotected", table_name);
    }
    else
    {
        elog(NOTICE, "Table '%s' is protected", table_name);
    }
    
   
    systable_endscan(scan);
    table_close(rel, AccessShareLock);
    
    return result;
}

static bool
is_protected_ddl_command(Node *parsetree, char **table_name)
{
    *table_name = NULL;
    
    if (!parsetree)
        return false;
    
    
    if (nodeTag(parsetree) == T_DropStmt)
    {
        DropStmt *stmt = (DropStmt *) parsetree;
        
        if (stmt->removeType == OBJECT_TABLE && stmt->objects && list_length(stmt->objects) > 0)
        {
            
            List *first_name = (List *) linitial(stmt->objects);
            if (list_length(first_name) > 0)
            {
                *table_name = pstrdup(strVal(llast(first_name)));
                elog(NOTICE, "DROP operation detected on table: %s", *table_name);
                return true;
            }
        }
    }
    else if (nodeTag(parsetree) == T_AlterTableStmt)
    {
        AlterTableStmt *stmt = (AlterTableStmt *) parsetree;
        if (stmt->relation && stmt->relation->relname)
        {
            *table_name = pstrdup(stmt->relation->relname);
            elog(NOTICE, "ALTER operation detected on table: %s", *table_name);
            return true;
        }
    }
    
    return false;
}

static void
warden_post_parse_analyze(ParseState *pstate, Query *query, JumbleState *jstate)
{
    if (query->commandType == CMD_DELETE || query->commandType == CMD_UPDATE)
    {
        if (query->commandType == CMD_DELETE)
            elog(NOTICE, "DELETE query detected");
        else
            elog(NOTICE, "UPDATE query detected");
            
        intercept_dml_command(pstate, query);
    }
    
    if (prev_post_parse_analyze_hook)
        prev_post_parse_analyze_hook(pstate, query, jstate);
}

static void
intercept_dml_command(ParseState *pstate, Query *query)
{
    RangeTblEntry *rte;
    char *table_name;
    char *operation_type = (query->commandType == CMD_DELETE) ? "DELETE" : "UPDATE";
    
    if (!query || (query->commandType != CMD_DELETE && query->commandType != CMD_UPDATE) || 
        !query->rtable || list_length(query->rtable) < 1)
        return;
    
    rte = (RangeTblEntry *) linitial(query->rtable);
    if (!rte || rte->rtekind != RTE_RELATION)
        return;
    
    table_name = get_rel_name(rte->relid);
    if (!table_name)
        return;
    
    elog(NOTICE, "%s operation on table: %s", operation_type, table_name);
    
    if (!is_unprotected_table(table_name))
    {
        /* Log blocked operation before throwing error */
        log_operation_csv(operation_type, table_name, "BLOCKED", 
                         "Table is protected", pstate->p_sourcetext);
        
        ereport(ERROR,
            (errcode(ERRCODE_OBJECT_NOT_IN_PREREQUISITE_STATE),
             errmsg("%s operations are not allowed on table \"%s\"", 
                   operation_type, table_name),
             errhint("Use warden_unprotect('%s') to allow modification", 
                    table_name)));
    }
    
    /* Log allowed operation */
    log_operation_csv(operation_type, table_name, "ALLOWED", NULL, pstate->p_sourcetext);
}

static void 
process_utility(PlannedStmt *pstmt,
                const char *queryString,
                bool readOnlyTree,
                ProcessUtilityContext context,
                ParamListInfo params,
                QueryEnvironment *queryEnv,
                DestReceiver *dest,
                QueryCompletion *qc)
{
    Node *parsetree = pstmt->utilityStmt;
    char *table_name = NULL;
    
    elog(NOTICE, "process_utility hook called");
    
    nested_level++;
    
    if (is_protected_ddl_command(parsetree, &table_name))
    {
        if (table_name)
        {
            char *cmd_type = (nodeTag(parsetree) == T_DropStmt) ? "DROP" : "ALTER";
            
            if (!is_unprotected_table(table_name))
            {
                /* Log blocked operation before throwing error */
                log_operation_csv(cmd_type, table_name, "BLOCKED", 
                                 "Table is protected", queryString);
                
                ereport(ERROR,
                    (errcode(ERRCODE_OBJECT_NOT_IN_PREREQUISITE_STATE),
                     errmsg("%s operations are not allowed on table \"%s\"", 
                           cmd_type, table_name),
                     errhint("Use warden_unprotect('%s') to allow modification", 
                            table_name)));
            }
            
            /* Log allowed operation */
            log_operation_csv(cmd_type, table_name, "ALLOWED", NULL, queryString);
            
            pfree(table_name);
        }
    }
    
    /* No need for separate ALTER TABLE logging - already handled above */
    elog(NOTICE, "Passing command to standard processor");
    if (prev_ProcessUtility)
        prev_ProcessUtility(pstmt, queryString, readOnlyTree,
                          context, params, queryEnv, dest, qc);
    else
        standard_ProcessUtility(pstmt, queryString, readOnlyTree,
                              context, params, queryEnv, dest, qc);
    
    nested_level--;
    elog(NOTICE, "process_utility hook finished");
}

Datum
warden_protect(PG_FUNCTION_ARGS)
{
    text *tablename_text = PG_GETARG_TEXT_PP(0);
    char *tablename = text_to_cstring(tablename_text);
    Relation rel;
    SysScanDesc scan;
    HeapTuple tuple;
    ScanKeyData key[1];
    bool result = false;
    
    if (!get_relname_relid(tablename, get_namespace_oid("public", false)))
    {
        ereport(ERROR,
               (errcode(ERRCODE_UNDEFINED_TABLE),
                errmsg("table \"%s\" does not exist", tablename)));
    }
    
    rel = table_open(get_relname_relid("warden_unprotected_tables", 
                                   get_namespace_oid("public", false)), 
                RowExclusiveLock);
    
    ScanKeyInit(&key[0],
                1, 
                BTEqualStrategyNumber,
                F_TEXTEQ,
                CStringGetTextDatum(tablename));
    
    scan = systable_beginscan(rel, InvalidOid, false, NULL, 1, key);
    
    tuple = systable_getnext(scan);
    if (HeapTupleIsValid(tuple))
    {
        simple_heap_delete(rel, &tuple->t_self);
        result = true;
        ereport(NOTICE,
               (errmsg("table \"%s\" is now protected from DELETE, UPDATE, ALTER, and DROP", tablename)));
        
        /* Log protection action */
        log_operation_csv("PROTECT", tablename, "SUCCESS", NULL, "WARDEN PROTECT");
    }
    else
    {
        ereport(NOTICE,
               (errmsg("table \"%s\" is already protected", tablename)));
    }
    
    systable_endscan(scan);
    table_close(rel, RowExclusiveLock);
    
    PG_RETURN_BOOL(result);
}


Datum
warden_unprotect(PG_FUNCTION_ARGS)
{
    text *tablename_text = PG_GETARG_TEXT_PP(0);
    char *tablename = text_to_cstring(tablename_text);
    Relation rel;
    TupleDesc tupdesc;
    HeapTuple tuple;
    Datum values[3];
    bool nulls[3] = {false, false, false};
    bool result = false;
    
    if (!get_relname_relid(tablename, get_namespace_oid("public", false)))
    {
        ereport(ERROR,
               (errcode(ERRCODE_UNDEFINED_TABLE),
                errmsg("table \"%s\" does not exist", tablename)));
    }
    
    if (is_unprotected_table(tablename))
    {
        ereport(NOTICE,
               (errmsg("table \"%s\" is already unprotected", tablename)));
        PG_RETURN_BOOL(false);
    }
    
    rel = table_open(get_relname_relid("warden_unprotected_tables", 
                                   get_namespace_oid("public", false)), 
                RowExclusiveLock);
    
    tupdesc = RelationGetDescr(rel);
    
    values[0] = CStringGetTextDatum(tablename);
    values[1] = DirectFunctionCall1(now, (Datum) 0);  /* current timestamp */
    values[2] = CStringGetTextDatum(GetUserNameFromId(GetUserId(), false));
    
    
    tuple = heap_form_tuple(tupdesc, values, nulls);
    
    simple_heap_insert(rel, tuple);
    result = true;
    
    ereport(NOTICE,
           (errmsg("table \"%s\" is now unprotected and allows DELETE, UPDATE, ALTER, and DROP", tablename)));
    
    /* Log unprotection action */
    log_operation_csv("UNPROTECT", tablename, "SUCCESS", NULL, "WARDEN UNPROTECT");
    
    heap_freetuple(tuple);
    table_close(rel, RowExclusiveLock);
    
    PG_RETURN_BOOL(result);
}


Datum
warden_all_queries(PG_FUNCTION_ARGS)
{
    ReturnSetInfo   *rsinfo = (ReturnSetInfo *) fcinfo->resultinfo;
    TupleDesc	    tupdesc;
    Tuplestorestate *tupstore;
    MemoryContext   per_query_ctx;
    MemoryContext   oldcontext;
    Datum           values[8];  /* Now 8 columns instead of 9 */
    bool            nulls[8];
    char            log_path[MAXPGPATH];
    char            line_buffer[4096];
    FILE            *fp = NULL;
    int             i;

    /* Initialize nulls array */
    for (i = 0; i < 8; i++)
        nulls[i] = false;

    if (get_call_result_type(fcinfo, NULL, &tupdesc) != TYPEFUNC_COMPOSITE)
        elog(ERROR, "return type must be a row type");

    per_query_ctx = rsinfo->econtext->ecxt_per_query_memory;
    oldcontext = MemoryContextSwitchTo(per_query_ctx);
    tupstore = tuplestore_begin_heap(true, false, work_mem);

    rsinfo->returnMode = SFRM_Materialize;
    rsinfo->setResult = tupstore;
    rsinfo->setDesc = tupdesc;

    /* Build log file path */
    snprintf(log_path, sizeof(log_path), "%s/pg_warden_ops.csv", DataDir);
    
    /* Open CSV file */
    fp = fopen(log_path, "r");

    if (fp == NULL)
    {
        /* No log file yet - return empty result set */
        tuplestore_donestoring(tupstore);
        MemoryContextSwitchTo(oldcontext);
        PG_RETURN_NULL();
    }
    
    /* Read and parse each CSV line */
    while (fgets(line_buffer, sizeof(line_buffer), fp) != NULL)
    {
        char *fields[8];  /* Now 8 fields */
        char *ptr = line_buffer;
        int field_count = 0;
        bool in_quotes = false;
        char *field_start = ptr;
        
        /* Simple CSV parser */
        while (*ptr && field_count < 8)
        {
            if (*ptr == '"')
            {
                in_quotes = !in_quotes;
            }
            else if (*ptr == ',' && !in_quotes)
            {
                *ptr = '\0';
                fields[field_count++] = field_start;
                field_start = ptr + 1;
            }
            else if (*ptr == '\n' && !in_quotes)
            {
                *ptr = '\0';
                fields[field_count++] = field_start;
                break;
            }
            ptr++;
        }
        
        /* Add last field if we haven't reached 8 fields */
        if (field_count < 8 && field_start < ptr)
        {
            fields[field_count++] = field_start;
        }
        
        /* Skip incomplete lines */
        if (field_count != 8)
            continue;
        
        /* Remove quotes from quoted fields */
        for (i = 0; i < 8; i++)
        {
            char *field = fields[i];
            int len = strlen(field);
            if (len >= 2 && field[0] == '"' && field[len-1] == '"')
            {
                char *src;
                char *dst;
                
                field[len-1] = '\0';
                fields[i] = field + 1;
                
                /* Unescape double quotes */
                src = fields[i];
                dst = fields[i];
                while (*src)
                {
                    if (src[0] == '"' && src[1] == '"')
                    {
                        *dst++ = '"';
                        src += 2;
                    }
                    else
                    {
                        *dst++ = *src++;
                    }
                }
                *dst = '\0';
            }
        }
        
        /* Populate values array (removed database_name) */
        values[0] = CStringGetTextDatum(fields[0]); /* timestamp */
        values[1] = CStringGetTextDatum(fields[1]); /* operation_type */
        values[2] = CStringGetTextDatum(fields[2]); /* table_name */
        values[3] = CStringGetTextDatum(fields[3]); /* user_name */
        values[4] = Int32GetDatum(atoi(fields[4])); /* session_pid */
        values[5] = CStringGetTextDatum(fields[5]); /* status */
        
        /* Handle NULL blocked_reason */
        if (strlen(fields[6]) == 0)
        {
            nulls[6] = true;
        }
        else
        {
            nulls[6] = false;
            values[6] = CStringGetTextDatum(fields[6]); /* blocked_reason */
        }
        
        values[7] = CStringGetTextDatum(fields[7]); /* query_text */
        
        tuplestore_putvalues(tupstore, tupdesc, values, nulls);
    }
    
    fclose(fp);
    
    tuplestore_donestoring(tupstore);
    MemoryContextSwitchTo(oldcontext);
    
    PG_RETURN_NULL();
}