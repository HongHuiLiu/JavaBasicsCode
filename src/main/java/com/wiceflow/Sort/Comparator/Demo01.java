package com.wiceflow.Sort.Comparator;
/**
 * ����������������(����)�ıȽ�
 * Created by BF on 2017/9/19.
 */
public class Demo01 {

	public static void main(String[] args) {
		Integer  a ; //���ݻ����������ʹ�С
		Character ch; //����Unicode����˳��
		String str="abc"; //�������һ��������һ����ʼ��ʼ���Ӵ������س���֮��
		String str2 ="abcd123";  //���򷵻ص�һ������ȵ�unicode��֮��
		System.out.println(str.compareTo(str2));
		str ="abc";
		str2 ="acd";
		System.out.println(str.compareTo(str2));
		
		
		java.util.Date d ;  //�������ڵĳ��������Ƚ�
	}

}
