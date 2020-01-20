package es.upm.dit.fcon.clientes;

public class BankClient implements BankClientI{
	
	private int account;
	private String name;
	private int balance;
	
	public BankClient() {
		
	}
	
	
	@Override
	public int getAccount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAccount(int account) {
		this.account=account;
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		this.name=name;
		
	}

	@Override
	public int getBalance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBalance(int balance) {
		this.balance=balance;
		
	}
	

}
