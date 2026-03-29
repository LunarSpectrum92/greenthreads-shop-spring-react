import React, { useState, useContext, useEffect } from "react";
import {
  Image,
  Spinner,
  Container,
  Row,
  Col,
  Card,
  Button,
  Dropdown,
  DropdownButton,
  ButtonGroup,
} from "react-bootstrap";
import NavBar from "../Components/NavBar.jsx";
import reactLogo from "../assets/1.jpg";
import Footer from "../Components/Footer.jsx";
import { CartProductsContext } from "../Contexts/CartProductsContext.jsx";
import PaginationProducts from "../Components/PaginationProducts.jsx";
import useGetFetch from "../hooks/useGetFetch.jsx";
import useGetFetchWithCallback from '../hooks/useGetFetchWithCallback';

import { Link,useLocation } from "react-router-dom";




const MainPage = ({ token}) => {

  const location = useLocation();
  const { url } = location.state || {};


  const defaultUrl = "http://localhost:8222/api/products/product/all";
  const [finalUrl, setFinalUrl] = useState(defaultUrl);

  
  useEffect(() => {
    fetchData(finalUrl);
  }, [finalUrl]);


  const { data: products, loading, error,fetchData} = useGetFetchWithCallback(finalUrl, token);
  const { data: brands, loadingBrands, errorBrands } = useGetFetch(
    "http://localhost:8222/api/products/products/brand",
    token
  );
  const { data: categories, loadingCategories, errorCategories } = useGetFetch(
    "http://localhost:8222/api/categories/categories",
    token
  );


  const { addToCart } = useContext(CartProductsContext);
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


  useEffect(() => {
    const urlToUse = url || defaultUrl;
    setFinalUrl(urlToUse);
    fetchData(urlToUse);
  }, [url, fetchData]);








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









  const handleAddToCart = (product) => {
    addToCart(product);
    setAddedToCart((prev) => ({ ...prev, [product.productId]: true }));
    alert(`${product.productName} został dodany do koszyka!`);
  };

  const indexOfLastProduct = state.activePage * state.limit;
  const indexOfFirstProduct = indexOfLastProduct - state.limit;
  const displayProducts = state.data.slice(indexOfFirstProduct, indexOfLastProduct);

  return (
    <div className="min-vh-100">

      <NavBar token={token}/>
      <Container className="my-5">
        <h2 className="mb-4">Products</h2>

        {loadingBrands ? (
          <Spinner animation="border" />
        ) : errorBrands ? (
          <p>Błąd ładowania marek: {errorBrands}</p>
        ) : (

          <DropdownButton
            as={ButtonGroup}
            variant="outline-dark"
            title="brands"
            className="mb-4 me-2"
            onSelect={handleBrandFiltering}
          >
            <Dropdown.Item eventKey="All">
                All  
              </Dropdown.Item>
            {brands && brands.length > 0 ? (
              brands.map((brand) => (
                <Dropdown.Item key={brand} eventKey={brand}>
                  {brand}
                </Dropdown.Item>
              ))
            ) : (
              <Dropdown.Item disabled>No brands available</Dropdown.Item>
            )}
          </DropdownButton>
        )}


  

      {loadingCategories ? (
          <Spinner animation="border" />
        ) : errorCategories ? (
          <p>Błąd ładowania kategorii: {errorCategories}</p>
        ) : (
          <DropdownButton
            as={ButtonGroup}
            variant="outline-dark"
            title="categories"
            className="mb-4 me-2"
            onSelect={handleCategoryFiltering}   
          >
              <Dropdown.Item eventKey="All">
                All  
              </Dropdown.Item>
            {categories && categories.length > 0 ? (
              categories.map((category) => (
                <Dropdown.Item key={category.categoryId} eventKey={category.categoryId}>
                  {category.categoryName}
                </Dropdown.Item>
              ))

            ) : (
              <Dropdown.Item disabled>No categories available</Dropdown.Item>
            )}

          </DropdownButton>
        )
        
        }
              

        {loading ? (
          <Spinner animation="border" />
        ) : error ? (
          <p>Błąd: {error}</p>
        ) : (
          <Row className="g-3 mb-2">
            {displayProducts.map((product) => (
              
              <Col md={4} sm={6} xs={12} key={product.productId}>
              <Link  to={{
                          pathname: `/product/${product.productId}`                          
                        }} style={{textDecoration: 'none'}}>

                <Card
                  className="bg-bg-light-subtle h-100 shadow-sm"
                  style={{ borderColor: 'beige', 
                    borderWidth: '1px',  // 1000px to zbyt dużo
                    borderStyle: 'solid', // wymagane dla widoczności ramki
                      
                  }}
                    
                >
                  <Card.Img variant="top" src={product.photoPaths[0] || reactLogo} style={{ borderColor: 'grey', 
  borderWidth: '2px',  // 1000px to zbyt dużo
  borderStyle: 'solid', // wymagane dla widoczności ramki
  objectFit: "cover"}} />
                  <Card.Body className="d-flex flex-column">
                    <Card.Title>{product.productName}</Card.Title>
                    <Card.Text className="">
                      {product.description}
                      <br />
                      <h5>{product.brand}</h5>
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

        <PaginationProducts
          paginationNumber={Math.ceil(state.data.length / state.limit)}
          activePage={state.activePage}
          onPageChange={handlePageChange}
        />
      </Container>
      <Footer />
    </div>
  );
};

export default MainPage;
