package es.upm.dit.fcon.clientes;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class InterfaceCli {
	
	private String idServer;
	
	public InterfaceCli(String idServer) {
		this.idServer =idServer;
	}

	public String interface_cli() {
		String res="";
		Scanner sn = new Scanner(System.in);
        boolean salir = false;
        String opcion; //Guardaremos la opcion del usuario
        String actualizacion;
        String idClient;
        while (!salir) {
        	System.out.println("Puede elegir entre: ");
            System.out.println("1. Dar de alta a un cliente");
            System.out.println("2. Actualizar informacion de un cliente");
            System.out.println("3. Obtener información de un cliente");
            System.out.println("4. Obtener el listado de clientes");
            System.out.println("5. Dar de baja a un cliente");
            System.out.println("6. Salir");
 
            try {
 
                System.out.println("Eliga la opcion que prefiera introduciendo su número");
                opcion = sn.nextLine();
                
                switch (opcion) {
                    case "1":
                        System.out.println("Introduce los datos del cliente");
                        Scanner c1 = new Scanner(System.in);
                        try {
                        	System.out.println("Nombre:");
                        	String name = c1.nextLine();
                        	System.out.println("Numero de cuenta:");
                        	int cuenta = c1.nextInt();
                        	System.out.println("Saldo en euros");
                        	int saldo = c1.nextInt();
                        	System.out.println("El cliente tendra el nombre: "+ name+" con un saldo de: " + saldo+ " en la cuenta: "+ cuenta);
                        	res = res+"create:"+name+";"+saldo+";"+cuenta;
                        	return res;
                        }catch (Exception e) {
                        	System.out.println("Ha ocurrido un error");
        	                
        	            }
                                               
                        break;
                    case "2":
                        System.out.println("Has seleccionado la opcion Actualizar cuenta");
                        Scanner s2 = new Scanner(System.in);
                        System.out.println("Introduce la el número de cuenta a actualizar");
                        idClient=s2.nextLine();
                        System.out.println("1. Modificar su saldo");
                        System.out.println("2. Modificar su numero de cuenta");
                        System.out.println("3. Modificar su propietario");
                        try {
                        	actualizacion = s2.nextLine();
                        	switch(actualizacion) {
                        		case "1":
                        			System.out.println("Introduza el nuevo saldo en euros");
                        			Scanner s3 = new Scanner(System.in);
                        			int nuevoSaldo = s3.nextInt();
                        			System.out.println("El nuevo saldo sera: " + nuevoSaldo + "€");
                        			res=res+"updateSaldo:"+"Ncuenta;"+idClient+";"+"saldo;"+nuevoSaldo;
                        			return res;
                        			
                        		case "2":
                        			System.out.println("Introduza el nuevo numero de cuenta");
                        			Scanner s4 = new Scanner(System.in);
                        			int nuevoNumero = s4.nextInt();
                        			System.out.println("El nuevo numero de cuenta sera: " + nuevoNumero);
                        			res=res+"updateCuenta:"+"Ncuenta;"+idClient+";"+"Ncuenta;"+nuevoNumero;
                        			return res;
                        		case "3":
                        			System.out.println("Introduza el nuevo nombre del propietario");
                        			Scanner s5 = new Scanner(System.in);
                        			String nuevoPropietario = s5.next();
                        			System.out.println("El nuevo propietario sera: " + nuevoPropietario);
                        			res=res+"updateNombre:"+"Ncuenta;"+idClient+";"+"name;"+nuevoPropietario;
                        			return res;
                 
                        	}
                        	
                        }catch (Exception e) {
                        	System.out.println("Ha ocurrido un error");
                        	
        	                salir=true;
        	            }
                        break;
                    case "3":
                        System.out.println("Introduzca el numero de cuenta a consultar: ");
                        Scanner c2 = new Scanner(System.in);
                        int consultaC = c2.nextInt();
                        System.out.println("Obteniendo datos de la cuenta: " +consultaC + " ...");
                        res=res+"read:"+consultaC;
                        return res;
                    case "4":
                        System.out.println("Obteniendo el listado de clientes...");
                        res=res+opcion;
                        return res;
                    case "5":
                    	Scanner c3 = new Scanner(System.in);
                        int eliminarC = c3.nextInt();
                        System.out.println("Se eliminará la cuenta: " +eliminarC);
                        res=res+"delete:"+eliminarC;
                        return res;
                    case "6":
                    	System.out.println("Cancelando operaciones " );
                    	res=res+"salir";
                        return res;
                    default:
                        System.out.println("Solo números entre 1 y 6");
                        return res;
                }
            } catch (InputMismatchException e) {
                System.out.println("Debes insertar un número");
                sn.next();
            }
        }
		return res;
	}
	
	
	
	
}
