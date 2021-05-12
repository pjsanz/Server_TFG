package Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import Entidades.DatosCliente;

public class Servidor {
	
	private ServerSocket ss;
	private ArrayList<String> 	     listaUsuarios;
	private ArrayList<DatosCliente>  listaDatosClientes;
	private Connection conn;
	
	public Servidor(int puerto){
	
			try {
				this.ss = new ServerSocket(puerto);
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
			//Realizamos la conexión con la BBDD mediante la libreria de JAVA mssql-jdbc
			
			try {
				  
	            String dbURL = "jdbc:sqlserver://DESKTOP-FVD51I9\\MSSQLSERVER:1433";
	            String user = "usuarioAPP";
	            String pass = "psswrd";
	            
	            this.conn = DriverManager.getConnection(dbURL, user, pass);	                      	          
	 
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        } 
			
			this.listaUsuarios = new ArrayList<String>();
			this.listaDatosClientes = new ArrayList<DatosCliente>();
	}
	
	public void inicio(){
		
		while(true){
			Socket s = null;
			
			try {
				s = ss.accept();
				Thread t = new Thread(new ManejadorPeticiones(listaDatosClientes,listaUsuarios,s, this.conn));
				t.start();
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
	
		}
	}
	
}