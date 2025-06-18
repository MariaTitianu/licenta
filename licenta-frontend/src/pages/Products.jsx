import { useState, useEffect, useRef } from 'react';
import { Typography, TextField, Button, Divider } from '@mui/material';
import { toast } from 'react-toastify';
import Ribbon from '../components/Ribbon';
import BackendSelectorWithToggle from '../components/BackendSelectorWithToggle';
import CustomTable from '../components/CustomTable';
import API from '../api';

const Products = ({ selectedBackend, setSelectedBackend }) => {
  const [products, setProducts] = useState([]);
  const [protectionStatus, setProtectionStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  // Form refs
  const idRef = useRef(null);
  const nameRef = useRef(null);
  const descriptionRef = useRef(null);
  const priceRef = useRef(null);
  const stockRef = useRef(null);

  // Fetch products on mount and backend change
  useEffect(() => {
    fetchProducts();
    fetchProtectionStatus();
  }, [selectedBackend]);

  const fetchProducts = async () => {
    try {
      const data = await API.products.getAll();
      setProducts(data);
    } catch (error) {
      toast.error('Failed to fetch products');
      console.error(error);
    }
  };

  const fetchProtectionStatus = async () => {
    try {
      const status = await API.protection.getStatus('products');
      setProtectionStatus(status.isProtected);
    } catch (error) {
      console.error('Failed to fetch protection status:', error);
    }
  };

  const handleRowClick = (params) => {
    const product = params.row;
    idRef.current.value = product.id;
    nameRef.current.value = product.name;
    descriptionRef.current.value = product.description;
    priceRef.current.value = product.price;
    stockRef.current.value = product.stockQuantity;
  };

  const clearForm = () => {
    idRef.current.value = '';
    nameRef.current.value = '';
    descriptionRef.current.value = '';
    priceRef.current.value = '';
    stockRef.current.value = '';
  };

  const handleCreate = async () => {
    const productData = {
      name: nameRef.current.value,
      description: descriptionRef.current.value,
      price: parseFloat(priceRef.current.value),
      stockQuantity: parseInt(stockRef.current.value)
    };

    if (!productData.name || !productData.price || !productData.stockQuantity) {
      toast.error('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      await API.products.create(productData);
      toast.success('Product created successfully');
      clearForm();
      fetchProducts();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
    } catch (error) {
      toast.error(error.message || 'Failed to create product');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    const id = idRef.current.value;
    if (!id) {
      toast.error('Please select a product to update');
      return;
    }

    const productData = {
      name: nameRef.current.value,
      description: descriptionRef.current.value,
      price: parseFloat(priceRef.current.value),
      stockQuantity: parseInt(stockRef.current.value)
    };

    setLoading(true);
    try {
      await API.products.update(id, productData);
      toast.success('Product updated successfully');
      fetchProducts();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
    } catch (error) {
      if (protectionStatus) {
        toast.error('Update blocked: Table is protected');
      } else {
        toast.error(error.message || 'Failed to update product');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    const id = idRef.current.value;
    if (!id) {
      toast.error('Please select a product to delete');
      return;
    }

    if (!window.confirm('Are you sure you want to delete this product?')) {
      return;
    }

    setLoading(true);
    try {
      await API.products.delete(id);
      toast.success('Product deleted successfully');
      clearForm();
      fetchProducts();
      // Refresh logs
      setTimeout(() => API.logs.getRecent(), 500);
    } catch (error) {
      if (protectionStatus) {
        toast.error('Delete blocked: Table is protected');
      } else {
        toast.error(error.message || 'Failed to delete product');
      }
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'name', headerName: 'Name', flex: 1 },
    { field: 'description', headerName: 'Description', flex: 2 },
    { field: 'price', headerName: 'Price', width: 100, valueFormatter: (value) => value != null ? `$${value}` : '$0' },
    { field: 'stockQuantity', headerName: 'Stock', width: 100 },
  ];

  return (
    <div className="page-container">
      <Ribbon />
      <div className="page-content">
        <div className="sidebar">
          <BackendSelectorWithToggle value={selectedBackend} onChange={setSelectedBackend} />
          
          <Typography variant="h5" gutterBottom>
            Products Management
          </Typography>

          <div className={`protection-badge ${protectionStatus ? 'protected' : 'unprotected'}`}>
            {protectionStatus ? 'PROTECTED' : 'UNPROTECTED'}
          </div>

          <Divider sx={{ my: 2 }} />

          <div className="form-fields">
            <TextField
              label="ID"
              size="small"
              inputRef={idRef}
              disabled
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="Name"
              size="small"
              inputRef={nameRef}
              required
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="Description"
              size="small"
              inputRef={descriptionRef}
              multiline
              rows={2}
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="Price"
              size="small"
              type="number"
              inputRef={priceRef}
              required
              InputLabelProps={{ shrink: true }}
              inputProps={{ step: 0.01 }}
            />
            <TextField
              label="Stock"
              size="small"
              type="number"
              inputRef={stockRef}
              required
              InputLabelProps={{ shrink: true }}
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
            title="Products"
            rows={products}
            columns={columns}
            onRowClick={handleRowClick}
            getRowId={(row) => row.id}
            height="100%"
          />
        </div>
      </div>
    </div>
  );
};

export default Products;