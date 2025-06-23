import { useState, useEffect, useRef } from 'react';
import { Typography, TextField, Button, Divider } from '@mui/material';
import { toast } from 'react-toastify';
import BackendSelectorWithToggle from '../components/BackendSelectorWithToggle';
import CustomTable from '../components/CustomTable';
import API from '../api';
import { useTheme } from '../context/ThemeContext';

const Payments = ({ selectedBackend, setSelectedBackend }) => {
  const { colors } = useTheme();
  const [payments, setPayments] = useState([]);
  const [protectionStatus, setProtectionStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  // Form refs
  const idRef = useRef(null);
  const customerNameRef = useRef(null);
  const cardTypeRef = useRef(null);
  const cardLastFourDigitsRef = useRef(null);
  const amountRef = useRef(null);
  const paymentDateRef = useRef(null);

  // Fetch payments on mount and backend change
  useEffect(() => {
    fetchPayments();
    fetchProtectionStatus();
  }, [selectedBackend]);

  const fetchPayments = async () => {
    try {
      const data = await API.payments.getAll();
      setPayments(data);
    } catch (error) {
      toast.error('Failed to fetch payments');
      console.error(error);
    }
  };

  const fetchProtectionStatus = async () => {
    try {
      const status = await API.protection.getStatus('customer_payments');
      setProtectionStatus(status.isProtected);
    } catch (error) {
      console.error('Failed to fetch protection status:', error);
    }
  };

  const handleRowClick = (params) => {
    const payment = params.row;
    idRef.current.value = payment.id;
    customerNameRef.current.value = payment.customerName;
    cardTypeRef.current.value = payment.cardType || '';
    cardLastFourDigitsRef.current.value = payment.cardLastFourDigits || '';
    amountRef.current.value = payment.amount;
    paymentDateRef.current.value = payment.paymentDate ? payment.paymentDate.split('T')[0] : '';
  };

  const clearForm = () => {
    idRef.current.value = '';
    customerNameRef.current.value = '';
    cardTypeRef.current.value = '';
    cardLastFourDigitsRef.current.value = '';
    amountRef.current.value = '';
    paymentDateRef.current.value = '';
  };

  const handleCreate = async () => {
    const paymentData = {
      customerName: customerNameRef.current.value,
      cardType: cardTypeRef.current.value,
      cardLastFourDigits: cardLastFourDigitsRef.current.value,
      amount: parseFloat(amountRef.current.value),
      paymentDate: paymentDateRef.current.value
    };

    if (!paymentData.customerName || !paymentData.amount || !paymentData.paymentDate || 
        !paymentData.cardType || !paymentData.cardLastFourDigits) {
      toast.error('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      await API.payments.create(paymentData);
      toast.success('Payment created successfully');
      clearForm();
      fetchPayments();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
    } catch (error) {
      toast.error(error.message || 'Failed to create payment');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    const id = idRef.current.value;
    if (!id) {
      toast.error('Please select a payment to update');
      return;
    }

    const paymentData = {
      customerName: customerNameRef.current.value,
      cardType: cardTypeRef.current.value,
      cardLastFourDigits: cardLastFourDigitsRef.current.value,
      amount: parseFloat(amountRef.current.value),
      paymentDate: paymentDateRef.current.value
    };

    setLoading(true);
    try {
      await API.payments.update(id, paymentData);
      toast.success('Payment updated successfully');
      fetchPayments();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
    } catch (error) {
      if (protectionStatus) {
        toast.error('Update blocked: Table is protected');
      } else {
        toast.error(error.message || 'Failed to update payment');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    const id = idRef.current.value;
    if (!id) {
      toast.error('Please select a payment to delete');
      return;
    }

    if (!window.confirm('Are you sure you want to delete this payment?')) {
      return;
    }

    setLoading(true);
    try {
      await API.payments.delete(id);
      toast.success('Payment deleted successfully');
      clearForm();
      fetchPayments();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
    } catch (error) {
      if (protectionStatus) {
        toast.error('Delete blocked: Table is protected');
      } else {
        toast.error(error.message || 'Failed to delete payment');
      }
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'customerName', headerName: 'Customer Name', flex: 1.5, minWidth: 180 },
    { field: 'cardType', headerName: 'Card Type', flex: 0.8, minWidth: 120 },
    { field: 'cardLastFourDigits', headerName: 'Card Last 4', width: 100 },
    { 
      field: 'amount', 
      headerName: 'Amount', 
      width: 120, 
      renderCell: (params) => params.value != null ? `$${params.value.toFixed(2)}` : '' 
    },
    { 
      field: 'paymentDate', 
      headerName: 'Payment Date', 
      flex: 1, 
      minWidth: 150,
      renderCell: (params) => params.value ? new Date(params.value).toLocaleDateString() : ''
    },
  ];

  // Common TextField styling for dark mode
  const textFieldSx = {
    '& .MuiInputLabel-root': { color: colors.textSecondary },
    '& .MuiOutlinedInput-root': { 
      color: colors.text,
      '& fieldset': { borderColor: colors.border },
      '&:hover fieldset': { borderColor: colors.accent },
      '&.Mui-focused fieldset': { borderColor: colors.accent },
      '&.Mui-disabled': {
        '& fieldset': { borderColor: colors.border },
      }
    },
    '& .Mui-disabled': { color: colors.textSecondary }
  };

  return (
    <div className="page-container">
      <div className="page-content">
        <div className="sidebar">
          <BackendSelectorWithToggle value={selectedBackend} onChange={setSelectedBackend} />
          
          <Typography variant="h5" gutterBottom style={{ color: colors.text }}>
            Payments Management
          </Typography>

          <div className={`protection-badge ${protectionStatus === null ? 'loading' : protectionStatus ? 'protected' : 'unprotected'}`}>
            {protectionStatus === null ? 'CHECKING...' : protectionStatus ? 'PROTECTED' : 'UNPROTECTED'}
          </div>

          <Divider sx={{ my: 2, borderColor: colors.border }} />

          <div className="form-fields">
            <TextField
              label="ID"
              size="small"
              inputRef={idRef}
              disabled
              InputLabelProps={{ shrink: true }}
              sx={textFieldSx}
            />
            <TextField
              label="Customer Name"
              size="small"
              inputRef={customerNameRef}
              required
              InputLabelProps={{ shrink: true }}
              sx={textFieldSx}
            />
            <TextField
              label="Card Type"
              size="small"
              inputRef={cardTypeRef}
              required
              InputLabelProps={{ shrink: true }}
              sx={textFieldSx}
            />
            <TextField
              label="Card Last 4 Digits"
              size="small"
              inputRef={cardLastFourDigitsRef}
              required
              InputLabelProps={{ shrink: true }}
              inputProps={{ maxLength: 4 }}
              sx={textFieldSx}
            />
            <TextField
              label="Amount"
              size="small"
              type="number"
              inputRef={amountRef}
              required
              InputLabelProps={{ shrink: true }}
              inputProps={{ step: 0.01 }}
              sx={textFieldSx}
            />
            <TextField
              label="Payment Date"
              size="small"
              type="date"
              inputRef={paymentDateRef}
              required
              InputLabelProps={{ shrink: true }}
              sx={textFieldSx}
            />
          </div>

          <div className="button-group">
            <Button 
              variant="contained" 
              color="primary" 
              onClick={handleCreate}
              disabled={loading}
            >
              Create
            </Button>
            <Button 
              variant="contained" 
              color="primary" 
              onClick={handleUpdate}
              disabled={loading}
            >
              Update
            </Button>
            <Button 
              variant="contained" 
              color="error" 
              onClick={handleDelete}
              disabled={loading}
            >
              Delete
            </Button>
          </div>
        </div>

        <div className="main-area">
          <CustomTable
            title="Customer Payments"
            rows={payments}
            columns={columns}
            onRowClick={handleRowClick}
            getRowId={(row) => row.id}
          />
        </div>
      </div>
    </div>
  );
};

export default Payments;