package model;


public class Curvelinear {

	private double a,  b;
	private String serviceName;
	
	public Curvelinear(String serviceName) {
		this.serviceName= serviceName;
	}
	
	
	
	 public Curvelinear(double a, double b, String serviceName) {
		this.a = a;
		this.b = b;
		this.serviceName = serviceName;
	}



	public double linearFunction( double x) {
		return a*x+b;
	}

	public void setAB(double a, double b) {
		this.a = a;
		this.b = b;
	}

	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
	}

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public String toString() {
		return "["+serviceName+ "=> y=" + a + "*x+" + b + "]";
	}
	

	 
}
