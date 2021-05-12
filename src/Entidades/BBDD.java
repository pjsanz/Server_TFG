package Entidades;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BBDD {
	
	private Connection conn ;
	
	public BBDD(Connection conn){		
		this.conn = conn;		
	}
	
	public Boolean insertarUsuario(String usuario, String contrase�a) {
		
		//Realizamos el insert del nuevo usuario en la BBDD si todo ha ido bien devolver� true, sino false
		
		try {
			  
			Statement st = conn.createStatement();
	           
            String INSERT = "INSERT INTO [Usuarios].[dbo].[usuarios] "
            				+ "VALUES ('"+ usuario +"Usuario','"+ contrase�a +"',getdate())";

            st.execute(INSERT);         
 
        	return true;
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
	}
	
	public String existeUsuario(String usuario) {
		
		//Comprobamos si existe el usuario, si es as� nos devolver� su clave y podremos hacer la comparativa, 
		//si no existe devolver� un string vacio y por tanto querr� decir que no existe
		
		try {
			  
			Statement st = conn.createStatement();
			
			String SELECT = "SELECT * FROM [Usuarios].[dbo].[usuarios]";
            
            ResultSet res = st.executeQuery(SELECT); 
            
            String usuarioBBDD = "";
            
            while(res.next()) {
            	
            	usuarioBBDD = res.getString("usuario"); 
            	
            	if(usuario.equals(usuarioBBDD)) {
            		return res.getString("contrase�a"); 
            	}

            }
					
        	return "";
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "error";
        }
	}			
	
}
