import React, { useEffect, useState, useRef } from "react";
import Keycloak from "keycloak-js";


const client = new Keycloak({
    url: import.meta.env.VITE_KEYCLOAK_URL,
    realm: import.meta.env.VITE_KEYCLOAK_REALM,
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT,
  });

  const useAuth = () => {
    const isRun = useRef(false);
    const [token, setToken] = useState(null);
    const [roles, setRoles] = useState(null);
    const [isLogin, setLogin] = useState(false);
  
    useEffect(() => {
      if (isRun.current) return;
    
      isRun.current = true;
      client
        .init({
          onLoad: "login-required",
        })
        .then((res) => {
          setLogin(res);
          setToken(client.token);
    
          const userRoles = client.resourceAccess?.AuthService?.roles || [];
          console.log("Załadowane role:", userRoles);
          setRoles(userRoles);
        })
        .catch((err) => {
          console.error("Błąd logowania:", err);
        });
    }, []);
    
  
    return [client, isLogin, token, roles];
  };
export default useAuth;
