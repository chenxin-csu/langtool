package test;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

public class Test {
	public static void main(String[] args) {
		B1 b1 = new B1();
		b1.setA(100);

		B2 b1c = new B2();

		try {
			BeanUtils.copyProperties(b1, b1c);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(b1);
		System.out.println(b1c.getA());
	}
}
class B3 {
	private int a = 0;

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	@Override
	public String toString() {
		return "B1 [a=" + a + "]";
	}
}
