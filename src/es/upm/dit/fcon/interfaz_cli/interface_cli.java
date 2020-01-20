package es.upm.dit.fcon.interfaz_cli;

import java.util.InputMismatchException;
import java.util.Scanner;

public class interface_cli {
	
	 public static void main(String[] args) {
		 
	        Scanner sn = new Scanner(System.in);
	        boolean salir = false;
	        String opcion; //Guardaremos la opcion del usuario
	        String actualizacion;
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
	                        	salir=true;
	                        }catch (Exception e) {
	                        	System.out.println("Ha ocurrido un error");
	        	                
	        	            }
	                        
	                        
	                        
	                        break;
	                    case "2":
	                        System.out.println("Has seleccionado la opcion Actualizar cuenta");
	                        Scanner s2 = new Scanner(System.in);
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
	                        			salir=true;
	                        			break;
	                        		case "2":
	                        			System.out.println("Introduza el nuevo numero de cuenta");
	                        			Scanner s4 = new Scanner(System.in);
	                        			int nuevoNumero = s4.nextInt();
	                        			System.out.println("El nuevo numero de cuenta sera: " + nuevoNumero);
	                        			salir=true;
	                        			break;
	                        		case "3":
	                        			System.out.println("Introduza el nuevo nombre del propietario");
	                        			Scanner s5 = new Scanner(System.in);
	                        			int nuevoPropietario = s5.nextInt();
	                        			System.out.println("El nuevo propietario sera: " + nuevoPropietario);
	                        			salir=true;
	                        			break;
	                 
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
	                        salir = true;
	                        break;
	                    case "4":
	                        System.out.println("Obteniendo el listado de clientes...");
	                        salir = true;
	                        break;
	                    case "5":
	                    	Scanner c3 = new Scanner(System.in);
	                        int eliminarC = c3.nextInt();
	                        System.out.println("Se eliminará la cuenta: " +eliminarC);
	                        salir = true;
	                        break;
	                    case "6":
	                    	System.out.println("Cancelando operaciones " );
	                        salir = true;
	                        break;
	                    default:
	                        System.out.println("Solo números entre 1 y 6");
	                }
	            } catch (InputMismatchException e) {
	                System.out.println("Debes insertar un número");
	                sn.next();
	            }
	        }
	 
	    }
}
