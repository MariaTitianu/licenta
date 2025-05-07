#include "/usr/include/postgresql/16/server/postgres.h"

#include <math.h>
#include <sys/stat.h>
#include <unistd.h>
#include <setjmp.h>

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

PG_MODULE_MAGIC;

void _PG_init(void);
void _PG_fini(void);


static int nested_level = 0;
static ProcessUtility_hook_type prev_ProcessUtility = NULL;
static post_parse_analyze_hook_type prev_post_parse_analyze_hook = NULL;


static void write_file(const char *str);
static bool is_alter_table_command(const char *query_string);
static bool is_unprotected_table(const char *table_name);
static void intercept_dml_command(ParseState *pstate, Query *query);
static bool is_protected_ddl_command(Node *parsetree, char **table_name);


Datum pg_all_queries(PG_FUNCTION_ARGS);
Datum pg_protect_table(PG_FUNCTION_ARGS);
Datum pg_unprotect_table(PG_FUNCTION_ARGS);

PG_FUNCTION_INFO_V1(pg_all_queries);
PG_FUNCTION_INFO_V1(pg_protect_table);
PG_FUNCTION_INFO_V1(pg_unprotect_table);

static void process_utility(PlannedStmt *pstmt,
                            const char *queryString,
                            bool readOnlyTree,
                            ProcessUtilityContext context,
                            ParamListInfo params,
                            QueryEnvironment *queryEnv,
                            DestReceiver *dest,
                            QueryCompletion *qc);


static void pg_log_post_parse_analyze(ParseState *pstate, Query *query, JumbleState *jstate);

void
_PG_init(void)
{
    elog(NOTICE, "pg_log extension initializing");
    
    
    prev_ProcessUtility = ProcessUtility_hook;
    ProcessUtility_hook = process_utility;
    
    prev_post_parse_analyze_hook = post_parse_analyze_hook;
    post_parse_analyze_hook = pg_log_post_parse_analyze;
    
    elog(NOTICE, "pg_log hooks installed successfully");
}

void
_PG_fini(void)
{
    elog(NOTICE, "pg_log extension unloading");
    ProcessUtility_hook = prev_ProcessUtility;
    post_parse_analyze_hook = prev_post_parse_analyze_hook;
}

static void
write_file(const char *str)
{
    FILE *fp = fopen("/tmp/pg_protected_ops.log", "a+");
    if (fp == NULL)
        elog(ERROR, "log: unable to open log file");
    fputs(str, fp);
    fputs("\n", fp);
    fclose(fp);
}


static bool
is_alter_table_command(const char *query_string)
{
    
    while (*query_string && isspace((unsigned char) *query_string))
        query_string++;
        

    return pg_strncasecmp(query_string, "ALTER TABLE", 11) == 0;
}


static bool
is_unprotected_table(const char *table_name)
{
    bool result = false;
    Relation rel;
    SysScanDesc scan;
    HeapTuple tuple;
    ScanKeyData key[1];
    
    elog(NOTICE, "checking if table '%s' is unprotected", table_name);
    
   
    if (strcmp(table_name, "pg_unprotected_tables") == 0)
    {
        elog(NOTICE, "pg_unprotected_tables is always unprotected");
        return true;
    }
    
  
    rel = table_open(get_relname_relid("pg_unprotected_tables", 
                                  get_namespace_oid("public", false)), 
                AccessShareLock);
    if (rel == NULL)
    {
        elog(WARNING, "Failed to open pg_unprotected_tables");
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
pg_log_post_parse_analyze(ParseState *pstate, Query *query, JumbleState *jstate)
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
        ereport(ERROR,
            (errcode(ERRCODE_OBJECT_NOT_IN_PREREQUISITE_STATE),
             errmsg("%s operations are not allowed on table \"%s\"", 
                   operation_type, table_name),
             errhint("Use pg_unprotect_table('%s') to allow modification", 
                    table_name)));
    }
    
    char log_message[1024];
    snprintf(log_message, sizeof(log_message), "ALLOWED %s ON %s", operation_type, table_name);
    write_file(log_message);
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
            if (!is_unprotected_table(table_name))
            {
                char *cmd_type = (nodeTag(parsetree) == T_DropStmt) ? "DROP" : "ALTER";
                
                ereport(ERROR,
                    (errcode(ERRCODE_OBJECT_NOT_IN_PREREQUISITE_STATE),
                     errmsg("%s operations are not allowed on table \"%s\"", 
                           cmd_type, table_name),
                     errhint("Use pg_unprotect_table('%s') to allow modification", 
                            table_name)));
            }
            
            char log_message[1024];
            char *cmd_type = (nodeTag(parsetree) == T_DropStmt) ? "DROP" : "ALTER";
            snprintf(log_message, sizeof(log_message), "ALLOWED %s ON %s", cmd_type, table_name);
            write_file(log_message);
            
            pfree(table_name);
        }
    }
    
    if (queryString && is_alter_table_command(queryString))
    {
        write_file(queryString);
        elog(NOTICE, "ALTER TABLE detected: %s", queryString);
    }
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
pg_protect_table(PG_FUNCTION_ARGS)
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
    
    rel = table_open(get_relname_relid("pg_unprotected_tables", 
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
        
        char log_message[1024];
        snprintf(log_message, sizeof(log_message), "PROTECTED TABLE %s", tablename);
        write_file(log_message);
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
pg_unprotect_table(PG_FUNCTION_ARGS)
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
        PG_RETURN_BOOL(true);
    }
    
    rel = table_open(get_relname_relid("pg_unprotected_tables", 
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
    
    char log_message[1024];
    snprintf(log_message, sizeof(log_message), "UNPROTECTED TABLE %s", tablename);
    write_file(log_message);
    
    heap_freetuple(tuple);
    table_close(rel, RowExclusiveLock);
    
    PG_RETURN_BOOL(result);
}


Datum
pg_all_queries(PG_FUNCTION_ARGS)
{
    ReturnSetInfo   *rsinfo = (ReturnSetInfo *) fcinfo->resultinfo;
    TupleDesc	    tupdesc;
    Tuplestorestate *tupstore;
    MemoryContext   per_query_ctx;
    MemoryContext   oldcontext;
    Datum           values[2];
    bool            nulls[2] = {false, false};
    char            pid[25];
    char            query_buffer[1024];
    FILE            *fp = NULL;
    bool            file_exists = false;
    struct stat     stat_buf;

    if (stat("/tmp/pg_protected_ops.log", &stat_buf) == 0) {
        file_exists = true;
    }

    if (get_call_result_type(fcinfo, NULL, &tupdesc) != TYPEFUNC_COMPOSITE)
        elog(ERROR, "return type must be a row type");

    per_query_ctx = rsinfo->econtext->ecxt_per_query_memory;
    oldcontext = MemoryContextSwitchTo(per_query_ctx);
    tupstore = tuplestore_begin_heap(true, false, work_mem);

    rsinfo->returnMode = SFRM_Materialize;
    rsinfo->setResult = tupstore;
    rsinfo->setDesc = tupdesc;

    if (file_exists) {
        fp = fopen("/tmp/pg_protected_ops.log", "r");
    }

    if (fp == NULL) {
        snprintf(query_buffer, sizeof(query_buffer), "no operations logged");
        snprintf(pid, sizeof(pid), "invalid pid");
        
        values[0] = CStringGetTextDatum(query_buffer);
        values[1] = CStringGetTextDatum(pid);
        
        tuplestore_putvalues(tupstore, tupdesc, values, nulls);
    } else {
        snprintf(pid, sizeof(pid), "%d", (int)getpid());
        
        while (fgets(query_buffer, sizeof(query_buffer) - 1, fp) != NULL) {
            size_t len = strlen(query_buffer);
            if (len > 0 && query_buffer[len-1] == '\n') {
                query_buffer[len-1] = '\0';
            }
            
            values[0] = CStringGetTextDatum(query_buffer);
            values[1] = CStringGetTextDatum(pid);
            
            tuplestore_putvalues(tupstore, tupdesc, values, nulls);
        }
        
        fclose(fp);
    }

    tuplestore_donestoring(tupstore);
    MemoryContextSwitchTo(oldcontext);
    
    return (Datum) 0;
}