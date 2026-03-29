import React, { useState, useEffect } from "react";
import {
  Spinner,
  Container,
  Row,
  Col,
  Card,
  Button,
  Accordion
} from "react-bootstrap";
import NavBar from "../Components/NavBar.jsx";
import reactLogo from "../assets/1.jpg";
import Footer from "../Components/Footer.jsx";
import { CartProductsContext } from "../Contexts/CartProductsContext.jsx";
import PaginationProducts from "../Components/PaginationProducts.jsx";
import useGetFetch from "../hooks/useGetFetch.jsx";
import { Link } from "react-router-dom";
import PutProductForm from './PutProductForm.jsx';

const MainPage = ({ token }) => {
  const defaultUrl = "http://localhost:8222/api/products/product/all";
  const [showA, setShowA] = useState(true);
  const { data: products, loading, error } = useGetFetch(defaultUrl, token);

  const [state, setState] = useState({
    data: [],
    limit: 9,
    activePage: 1,
  });
  const toggleShowA = () => setShowA(!showA);
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



  const photoPathsSplit = (paths) => {

    let ids = [];
    paths.map((path) => {
      ids.push(path.split('/').pop());
    })
    return ids;
  }


  const indexOfLastProduct = state.activePage * state.limit;
  const indexOfFirstProduct = indexOfLastProduct - state.limit;
  const displayProducts = state.data.slice(indexOfFirstProduct, indexOfLastProduct);
console.log(products)
  return (
    <>
      <Container className="my-5">
        {loading ? (
          <Spinner animation="border" />
        ) : error ? (
          <p>Błąd: {error}</p>
        ) : ( 
          <Row className="g-3 mb-2">
            {displayProducts.map((product) => ( 
              <Col md={12} sm={12} xs={12} key={product.productId}>
                  <Card className="h-100 border-1 ">
                    <Row noGutters>
                      <Col md={4} sm={12} xs={12}>
                        <Card.Img
                          variant="top"
                          src={product.photoPaths[0] || reactLogo}
                          style={{
                            width: "100%",
                            height: "auto",
                            objectFit: "cover",
                          }}
                        />
                      </Col>
                      <Col md={8} sm={12} xs={12}>
                        <Card.Body>
                          <Card.Text>
                            <strong>{product.productName}</strong>
                            <br />
                            {product.description}
                            <br />
                            {product.brand}
                            <br />
                            quantity: {product.quantity}
                            <br />
                              {product.category.map((categorie, index) => (
                                <p> {categorie}</p>
                              ))}
                            <br />
                            <b>{product.price} PLN</b>
                          </Card.Text>
                        </Card.Body>
                      </Col>
                    </Row>
                    <Accordion className="border-0">
                      <Accordion.Item eventKey="0" className="border-0">
                        <Accordion.Header>update product</Accordion.Header>
                        <Accordion.Body>
                            <PutProductForm token={token} url={`http://localhost:8222/api/products/product/put`} productId={product.productId} photoIds={photoPathsSplit(product.photoPaths)}/>
                        </Accordion.Body>
                      </Accordion.Item>
                    </Accordion>
                  </Card>
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
    </>
  );
};

export default MainPage;
