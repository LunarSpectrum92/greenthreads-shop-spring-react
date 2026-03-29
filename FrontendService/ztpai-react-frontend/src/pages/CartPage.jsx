import reactLogo from "../assets/1.jpg";
import React, { useContext, useEffect, useState } from 'react';
import { Container, Card, Button, Badge } from 'react-bootstrap';
import NavBar from '../Components/NavBar.jsx';
import Footer from '../Components/Footer.jsx';
import {CartProductsContext} from '../Contexts/CartProductsContext.jsx';
import Timer from "../Components/Timer";
import usePostFetch from "../hooks/usePostFetch.jsx";
import { Link, useNavigate } from "react-router-dom";


const CartPage = ({ token, client }) => {
  const { cart, addToCart, removeQuantity } = useContext(CartProductsContext);
  const [keycloakClientData, setKeycloakClientData] = useState(null);
  const [orderData, setOrderData] = useState({
    "clientId" : "",
    "orderProductsList": [{
      "productId": "",
      "quantity": "",
      "price": ""  
    }]
   });
   const navigate = useNavigate();
const { data: response, loading, error, sendRequest } = usePostFetch(`http://localhost:8222/api/orders/order`, token);
  

  useEffect(() => {
    if (token && client?.idTokenParsed) {
        setKeycloakClientData(client.idTokenParsed);
    }
}, [token, client]);

const handleSubmit = () => {
  if(!keycloakClientData) return;
 
  const updatedOrderData = {
    clientId: keycloakClientData.sub,
    orderProductsList: cart.map(product => ({
      productId: product.productId,
      quantity: product.quantityCart,
      price: product.price * product.quantityCart
    }))
  };

  setOrderData(updatedOrderData);
  console.log(updatedOrderData);


  console.log(updatedOrderData)
  sendRequest(updatedOrderData);

} 

useEffect(() => {
  
  if (response) {
    navigate('/payment', { state: {orderId: response.orderId, customerId: response.clientId, }}); 
  }

  if (error) {
    console.error("Error while sending request", error);
  }

}, [response, error, navigate]);




  return (
    <div className="d-flex flex-column min-vh-100">
      <NavBar token={token}/>
      <Timer/>
      <Container className="my-5 d-flex flex-column flex-grow-1">
        <h1>
          <Badge bg="secondary" className="my-2">
            Cart
          </Badge>
        </h1>
        {!cart || cart.length === 0 ? (
          <p>Your cart is empty.</p>
        ) : (
          cart.map((product) => (
            <Card className="my-3" key={product.productId}>
              <Card.Header as="h5">{product.discount ? 'Featured' : 'Product'}</Card.Header>
              <Card.Body className="d-md-flex align-items-center justify-content-between">
                <div className="d-md-flex align-items-center">
                  <div className="w-25" style={{ padding: '3px', marginInlineEnd: '10px'}}>
                    <Card.Img variant="top" src={product.image || reactLogo}  />
                  </div>
                  <div>
                    <Card.Title>{product.productName}</Card.Title>
                    <Card.Text>{product.description}</Card.Text>
                    <p>
                      <b>{product.price} PLN</b>
                    </p>
                  </div>
                </div>
                <div className="d-md-flex align-items-center justify-content-between">
                  <Badge bg="secondary" className="me-2">
                    {product.quantityCart}
                  </Badge>
                  <Button
                    variant="outline-dark"
                    className="me-1"
                    style={{ width: '40px' }}
                    onClick={() => addToCart(product)}
                  >
                    +
                  </Button>
                  <Button
                    variant="outline-dark"
                    className="ms-1"
                    style={{ width: '40px' }}
                    onClick={() => removeQuantity(product.productId)}
                  >
                    -
                  </Button>
                </div>
              </Card.Body>
            </Card>
          ))
        )}
        {cart.length != 0 ? 
        
        
        loading ? 
        <Button
        variant="outline-dark"
        className="ms-1"
        type='submit'
        onClick={handleSubmit}
          disabled
      >
        ...Processing
      </Button>
        :
        <Button
        variant="outline-dark"
        className="ms-1"
        type='submit'
        onClick={handleSubmit}
      >
        Order
      </Button> :

        <p></p>
        }
        
      </Container>
      <Footer />

    </div>
  );
};

export default CartPage;

