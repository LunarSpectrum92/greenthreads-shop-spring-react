import React, { useEffect, useContext, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Container,
  Row,
  Col,
  Card,
  Button,
  Form,
  Image,
  Carousel,
  Spinner
} from 'react-bootstrap';
import { CartProductsContext } from "../Contexts/CartProductsContext.jsx";

import NavBar from '../Components/NavBar.jsx';
import Footer from '../Components/Footer.jsx';
import reactLogo from "../assets/05BY4C572B40.jpg";
import { useNavigate } from "react-router-dom";
import useGetFetch from "../hooks/useGetFetch.jsx";
import useFetch from "../hooks/usePostFetch.jsx";

const ProductPage = ({ token, url, client }) => {
  const [cartQuantity, setCartQuantity] = useState(null);

  const { productId } = useParams();
  const navigate = useNavigate();
  const { addToCarMultiple } = useContext(CartProductsContext);
  const [keycloakClientData, setKeycloakClientData] = useState(null);
  const [products, setProducts] = useState(null);
  const [clientData, setClientData] = useState(null);
  const [newReview, setNewReview] = useState({ commentBody: '', score: 0 });
  const [product, setProduct] = useState({});
  const [comments, setComments] = useState([]);

  
  
  const handleAddToCart = (product, cartQuantity) => {
    if (cartQuantity <= 0 || cartQuantity > product.quantity) {
      alert("Invalid quantity selected!");
      return;
    }
    
    addToCarMultiple(product, cartQuantity);
    alert(`${product.productName} (${cartQuantity} szt.) was added to the cart!`);
  };

  const handleQuantityChange = (e) => {
    setCartQuantity(Math.max(0, Math.min(e.target.value, product.quantity))); 
  };


  const { data: productsFirst, loading: productsLoading, error: productsError } = useGetFetch(
    "http://localhost:8222/api/products/product/all", 
    token
  );

  const { data: commentsData, loading: commentsLoading, error: commentsError } = useGetFetch(
    product.productId ? `http://localhost:8222/api/comments/comment/product/${encodeURIComponent(product.productId)}` : null,
    token
  );

  const { data: clientResponseData, loading: clientLoading, error: clientError } = useGetFetch(
    keycloakClientData?.sub 
      ? `http://localhost:8222/api/clients/client/keycloak/${encodeURIComponent(keycloakClientData.sub)}`
      : null,
    token
  );

  const { sendRequest: postComment, loading: postCommentLoading, error: postCommentError } = useFetch(
    "http://localhost:8222/api/comments/comment", 
    token
  );

  useEffect(() => {
    if (clientError) {
      console.error('Error fetching client data:', clientError);
    }
    if (clientResponseData) {
      setClientData(clientResponseData);
    }
  }, [clientResponseData, clientError]);

  useEffect(() => {
    if (token && client?.idTokenParsed) {
      setKeycloakClientData(client.idTokenParsed);
    }
  }, [token, client]);

  useEffect(() => {
    if (productsFirst?.length > 0) {
      setProducts(productsFirst);
    }
  }, [productsFirst]);

  useEffect(() => {
    if (commentsData) {
      setComments(Array.isArray(commentsData) ? commentsData : []);
    }
  }, [commentsData]);

  useEffect(() => {
    if (!products?.length) return;
    
    const filteredProduct = products.find(p => p.productId === parseInt(productId));
    if (!filteredProduct) {
      navigate("/error", { replace: true });
      return;
    }
    setProduct(filteredProduct);
  }, [products, productId, navigate]);

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    
    if (!clientData?.userId) {
      alert("You must be logged in to submit a review.");
      return;
    }

    const reviewData = {
      commentBody: newReview.commentBody,
      score: parseInt(newReview.score),
      userId: clientData.userId,
      productId: product.productId,
    };

    try {
      const newComment = await postComment(reviewData);
      console.log(newComment)
      if (newComment) {
        alert("Review submitted successfully!");
        setNewReview({ commentBody: '', score: 0 });
        setComments(prevComments => Array.isArray(prevComments) ? [...prevComments, newComment] : [newComment]);
        window.location.reload();
      }
    } catch (err) {
      console.error("Error submitting review:", err);
      alert("Failed to submit review. Please try again.");
    }
  };

  if (productsLoading || clientLoading) {
    return (
      <div className="d-flex flex-column min-vh-100">
        <NavBar token={token}/>
        <Container className="d-flex justify-content-center align-items-center flex-grow-1">
          <Spinner animation="border" role="status" />
        </Container>
        <Footer />
      </div>
    );
  }

  if (productsError || clientError) {
    return (
      <div className="d-flex flex-column min-vh-100">
        <NavBar />
        <Container className="d-flex justify-content-center align-items-center flex-grow-1">
          <div className="text-center">
            <h3>Error loading data</h3>
            <p>Please try again later.</p>
          </div>
        </Container>
        <Footer />
      </div>
    );
  }

  const commentsList = Array.isArray(comments) ? comments : [];

  return (
    <div className="d-flex flex-column min-vh-100">
      <NavBar />
      <Container className="mt-5">
        <Row>
          <Col md={6}>
            <Carousel>
              {product?.photoPaths?.length > 0 ? (
                product.photoPaths.map((photo, index) => (
                  <Carousel.Item key={index}>
                    <Image src={photo} className="d-block w-100" alt={`Product view ${index + 1}`}  style={{
                            height: "600px",
                            objectFit: "cover",
                          }} />
                  </Carousel.Item>
                ))
              ) : (
                <Carousel.Item>
                  <Image src={reactLogo} className="d-block w-100" alt="Default product" style={{
                      maxWidth: "100%",
                      width: "auto",
                      height: "auto",
                      objectFit: "contain",
                          }} />
                </Carousel.Item>
              )}
            </Carousel>
          </Col>
          <Col md={6} className="d-flex flex-column justify-content-center">
            <h2>{product.productName}</h2>
            <h3>{product.price}</h3>
            <h5>{product.brand}</h5>

            <p>{product.description}</p>
            <Form>
      <Form.Group controlId="productQuantity">
        <Form.Label>Product Quantity: {product.quantity}</Form.Label>
        <Form.Control
          type="number"
          value={cartQuantity}  
          max={product.quantity}
          min={0}
          placeholder="Enter quantity"
          onChange={handleQuantityChange} 
        />
      </Form.Group>
      <Button
        variant="outline-dark"
        onClick={() => handleAddToCart(product, cartQuantity)} 
        style={{
          marginTop: '8px'
        }}
      >
        {"Dodaj do koszyka"}
      </Button>
    </Form>
          </Col>
        </Row>

        <h4 className="mt-5 mb-4">Latest reviews</h4>
        {commentsLoading ? (
          <Spinner animation="border" role="status" />
        ) : commentsList.length > 0 ? (
          commentsList.map((review, index) => (
            <Card key={index} className="mb-4">
              <Card.Body>
                <Card.Text>{review.commentBody}</Card.Text>
              </Card.Body>
              <Card.Footer className="text-muted">
                {review.userId} - {new Date(review.commentDate).toLocaleDateString()} - {'⭐'.repeat(review.score)}
              </Card.Footer>
            </Card>
          ))
        ) : (
          <p>No reviews available for this product.</p>
        )}


        <div className="comment-section bg-light py-4 px-3 mb-5 mt-5">
          <h4 className="mt-2 mb-5">Add Your Comment</h4>
          <Form onSubmit={handleSubmitReview}>
            <Form.Group controlId="reviewRating">
              <Form.Label>Rating</Form.Label>
              <Form.Control
                type="number"
                max={5}
                min={0}
                value={newReview.score}
                onChange={(e) => setNewReview({ ...newReview, score: e.target.value })}
              />
              <Form.Control
                as="textarea"
                className="mt-2"
                placeholder="Comment"
                rows={3}
                value={newReview.commentBody}
                onChange={(e) => setNewReview({ ...newReview, commentBody: e.target.value })}
              />
            </Form.Group>
            <Button 
              type="submit" 
              variant="dark" 
              className="mt-3" 
              disabled={postCommentLoading}
            >
              {postCommentLoading ? 'Submitting...' : 'Submit'}
            </Button>
          </Form>
        </div>
        </Container>
      <Footer />
    </div>
  );
};


export default ProductPage;