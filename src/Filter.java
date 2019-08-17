
public abstract class Filter {
	protected String name;
	protected int maskSize;
	
	public int applyFilter(int[][] originalImage) {
		return 0;
	}
	
	public String getName(){
		return name;
	}
	
	public int getMaskSize(){
		return maskSize;
	}
}
