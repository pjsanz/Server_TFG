package Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import Entidades.DatosCliente;

public class Servidor {
	
	private ServerSocket ss;
	private ArrayList<String> 				   listaUsuarios;
	private ArrayList<DatosCliente>  listaDatosClientes;
	
	public Servidor(int puerto){
	
			try {
				this.ss = new ServerSocket(puerto);
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			this.listaUsuarios = new ArrayList<String>();
			this.listaDatosClientes = new ArrayList<DatosCliente>();
	}
	
	public void inicio(){
		
		while(true){
			Socket s = null;
			
			try {
				s = ss.accept();
				Thread t = new Thread(new ManejadorPeticiones(listaDatosClientes,listaUsuarios,s));
				t.start();
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
	
		}
	}
	
}