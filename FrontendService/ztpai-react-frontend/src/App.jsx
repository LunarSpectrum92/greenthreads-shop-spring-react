  import 'bootstrap/dist/css/bootstrap.min.css';
  import useAuth from './hooks/useAuth.jsx';
  import MainPage from './pages/MainPage';
  import CartPage from './pages/CartPage';
  import ErrorPage from './pages/ErrorPage.jsx';
  import ProductPage from './pages/ProductPage.jsx';
  import ProductsPage from './pages/ProductsPage.jsx';
  import CartProductsContextProvider from './Contexts/CartProductsContext.jsx';
  import React, { useEffect, useState } from 'react';
  import {createBrowserRouter, RouterProvider} from 'react-router-dom'
  import AccontPage from './pages/AccontPage.jsx';
  import AboutUs from './pages/AboutUs.jsx';
  import AdminPage from './pages/AdminPage.jsx';
  import PaymentPage from './pages/PaymentPage.jsx';
  import ProtectedRoute from "./utils/ProtectedRoutes";



  const App = () => {

    const [roles1, setRoles] = useState([]);
    const [client, isLogin, token, roles] = useAuth();

  useEffect(() => {
    if (!roles || !Array.isArray(roles)) return;
    console.log("Zaktualizowane roles:", roles);
    setRoles(roles);
  }, [roles])




  const router = createBrowserRouter([
    {
      path: '/',
      element:  <MainPage token={token} /> ,
      errorElement: <ErrorPage token={token}/>,
    },
    {
      path: '/error',
      element: <ErrorPage token={token}/>,
    },
    {
      path: '/cart',
      element: <CartPage token={token} client={client}/>
    },
    {
      path: '/product/:productId',
      element: <ProductPage token={token} client={client}/>
    },{
      path: '/products', 
      element: <ProductsPage token={token}/>
    },{
      path: '/account',
      element: <AccontPage token={token} client={client}/>
    },{
      path: "/about",
      element: <AboutUs token={token} client={client}/>
     },
    
    {
      path: '/admin',
      element: roles1.length === 0 ? ( 
        <p>Ładowanie...</p>
      ) : (
        <ProtectedRoute roles={roles1} requiredRole="Admin">
          <AdminPage token={token} />
        </ProtectedRoute>
      ),
    },{
      path: "/payment",
      element: <PaymentPage token={token}/>
    }
  ])  

    return (

    <CartProductsContextProvider>
        <RouterProvider router={router}/>
    </CartProductsContextProvider>

  );
  };



  export default App
