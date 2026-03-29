import React from 'react';
import  NavBar  from '../Components/NavBar.jsx';
import Footer from '../Components/Footer.jsx';



const AboutUs = ({ token, client }) => {
  return (
    <div className="min-vh-100">
    <NavBar token={token}/>
    <div className="container-fluid  mt-3" style={{ }}>
          <div className="container">
              <h1 className="mb-5">About Us</h1>

              <div className="row">
                  <div className="col-md-6 mb-4">
                      <div className="p-4 bg-white shadow rounded">
                          <h5>Contact:</h5>
                          <p>Phone: 111 111 111</p>
                          <p>Email: example@example.com</p>

                          <h5>Locations:</h5>
                          <p>exampleLocation</p>
                          <p>exampleLocation</p>
                      </div>
                  </div>

                  <div className="col-md-6 mb-4">
                      <div
                          className="p-4 bg-white shadow rounded d-flex align-items-center justify-content-center"
                          style={{ height: '200px' }}
                      >
                          <span>Map</span>
                      </div>
                  </div>
              </div>
          </div>
      </div>
      <Footer />
      </div>
    
  );
};

export default AboutUs;