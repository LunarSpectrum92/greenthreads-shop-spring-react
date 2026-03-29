import React, { useState, useContext, useEffect } from 'react';
import {  Image, Container, Row, Col, Card,Button,  Carousel, Form ,Dropdown, DropdownButton ,ButtonGroup, Spinner  } from 'react-bootstrap';
import NavBar from '../Components/NavBar.jsx'
import reactLogo from "../assets/1.jpg";
import a from '../assets/5.jpg'
import b from '../assets/7.jpg'
import c from '../assets/3.jpg'
import d from '../assets/4.jpg'


import Footer from '../Components/Footer.jsx';
import {CartProductsContext} from '../Contexts/CartProductsContext.jsx';
import { products } from '../assets/products.js';
import ContactUs from '../Components/ContactUs.jsx';
import useGetFetch from "../hooks/useGetFetch.jsx";
import { Link } from "react-router-dom";
import PaginationProducts from "../Components/PaginationProducts.jsx";




const MainPage = ({token}) => {


  const {addToCart} = useContext(CartProductsContext);

  const defaultUrl = "http://localhost:8222/api/products/product/all";
  const [finalUrl, setFinalUrl] = useState(defaultUrl);



  useEffect(() => {
    if (defaultUrl) setFinalUrl(defaultUrl);
  }, []);

  const { data: products, loading, error } = useGetFetch(finalUrl, token);
  const { data: brands, loadingBrands, errorBrands } = useGetFetch(
    "http://localhost:8222/api/products/products/brand",
    token
  );
  const { data: categories, loadingCategories, errorCategories } = useGetFetch(
    "http://localhost:8222/api/categories/categories",
    token
  );


  const [state, setState] = useState({
    data: [],
    limit: 9,
    activePage: 1,
  });
  const [addedToCart, setAddedToCart] = useState({});

  useEffect(() => {
    if (products) {
      setState((prev) => ({
        ...prev,
        data: products,
      }));
    }
  }, [products]);

  const handlePageChange = (pageNumber) => {
    setState((prev) => ({
      ...prev,
      activePage: pageNumber,
    }));
  };

  const handleBrandFiltering = (brand) => {
    if(brand === "All"){
      setFinalUrl(
        `http://localhost:8222/api/products/product/all`
      );

      return
    }
    setFinalUrl(
      `http://localhost:8222/api/products/product/brand?brand=${encodeURIComponent(brand)}`
    );
  };


  const handleCategoryFiltering = (categoryId) => {
    if(categoryId === "All"){
      setFinalUrl(
        `http://localhost:8222/api/products/product/all`
      );

      return
    }
    setFinalUrl(
      `http://localhost:8222/api/products/product/category?categoryId=${encodeURIComponent(categoryId)}`
    );
  };



  const indexOfLastProduct = state.activePage * state.limit;
  const indexOfFirstProduct = indexOfLastProduct - state.limit;
  const displayProducts = state.data.slice(indexOfFirstProduct, indexOfLastProduct);



  
  const handleAddToCart = (product) => {
    alert("added to cart")
    addToCart(product);

  };



    return (
      <div className="min-vh-100">

        <NavBar token={token}/>    
      <div className="text-center py-5" style={{backgroundColor: "#f1e9db"}}>
        <Container>
        <Row>
          <Col md={6} className="order-2 order-md-1">
            <Carousel variant="dark">
              <Carousel.Item interval={10000}>
                <Image src={a}  style={{ height: '500px', objectFit: 'cover' }} className="" fluid />
              </Carousel.Item>
              <Carousel.Item interval={10000}>
                <Image src={b} style={{ height: '500px', objectFit: 'cover' }} className="" fluid />
              </Carousel.Item>
              <Carousel.Item interval={10000}>
                <Image src={c}  style={{ height: '500px', objectFit: 'cover' }} className="" fluid />
              </Carousel.Item>
            </Carousel>
          </Col>
            
          <div className="w-100 d-md-none"></div>
            
          <Col md={6} className="d-flex flex-column justify-content-center align-items-center order-1 order-md-2 py-4">
            <div>
              <h1 className="display-4 fw-bold">GreenThreads</h1>
              <p className="lead">Style That Loves Nature</p>
              <Button variant="outline-secondary me-2" size="lg" href="#about">
                Man
              </Button>
              <Button variant="dark" size="lg" href="#about">
                Woman
              </Button>
            </div>
          </Col>
        </Row>
        </Container>
      </div>

      
      <Container className="my-5 ">
        <h2 className=" mb-4">Newest</h2>


        <Row className="g-3 ">
        {loading ? (
          <Spinner animation="border" />
        ) : error ? (
          <p>Błąd: {error}</p>
        ) : (
          <Row className="g-3 mb-2">
            {displayProducts.map((product) => (
              
              <Col md={4} sm={6} xs={12} key={product.productId} >
              <Link  to={{
                          pathname: `/product/${product.productId}`                          
                        }} style={{  textDecoration:'none' }}>

                <Card
                  className="bg-bg-light-subtle h-100 shadow-sm"
                  style={{ border: "0px", textDecoration:'none' }}
                >
                  <Card.Img variant="top" src={product.photoPaths[0] || reactLogo} style={{ objectFit: "cover"}} />
                  <Card.Body className="d-flex flex-column">
                    <Card.Title>{product.productName}</Card.Title>
                    <Card.Text className="">
                      {product.description}
                      <br />
                      <p>
                        <b>{product.price}</b> PLN
                      </p>
                    </Card.Text>
                    <div className="mt-auto">
                      <Button
                        variant="outline-dark"
                        disabled={addedToCart[product.productId]}
                        onClick={() => handleAddToCart(product)}
                      >
                        {addedToCart[product.productId] ? "Dodano" : "Dodaj do koszyka"}
                      </Button>
                    </div>
                  </Card.Body>
                </Card>
              </Link>

              </Col>

            ))}
          </Row>
        )}

        </Row>
        <PaginationProducts
          paginationNumber={Math.ceil(state.data.length / state.limit)}
          activePage={state.activePage}
          onPageChange={handlePageChange}
        />
      </Container>

      <ContactUs/>

        <Footer/>
    </div>
  );
};

export default MainPage;
