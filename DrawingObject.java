package original;

import java.io.Serializable;

public class DrawingObject implements Serializable{
	private static final long serialVersionUID = 1L;
	int upperLeftX, upperLeftY;
	int width, height;
	int x1, y1, x2, y2;
	boolean fill = false;
	String color;
	String shape;
	
	DrawingObject(int upperLeftX, int upperLeftY, int width, int height,String shape,boolean fill,String c){
		this.upperLeftX = upperLeftX;
		this.upperLeftY = upperLeftY;
		this.width = width;
		this.shape = shape;
		this.height = height;
		this.fill = fill;
		this.color=c;
	}
	
	DrawingObject(int x1, int y1, int x2, int y2,String shape, String c){
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.shape = shape;
		this.color=c;
	}

	public DrawingObject() {
	}
}