
public class MaskFilter extends Filter{
	private float[][] mask;
	private boolean maskAverage;
	private int maskTotal;
	
	public MaskFilter(String newName, float[][] newMask){
		name = newName;
		mask = newMask;
		maskAverage = false;
		maskSize = newMask[0].length;
		maskTotal = 0;
	}
	
	public MaskFilter(String newName, float[][] newMask, boolean newMaskAverage){
		name = newName;
		mask = newMask;
		maskAverage = newMaskAverage;
		maskSize = newMask[0].length;
		for(int x = 0; x < maskSize; x++){
			for(int y = 0; y < maskSize; y++){
				maskTotal += mask[x][y];
			}
		}
	}
	
	public int applyFilter(int[][] originalImage){
		if(originalImage.length != maskSize || originalImage[0].length != maskSize) {
			System.out.println("Grid passed has wrong width/height");
			return 0;
		}
		if(originalImage.length != maskSize) {
			System.out.println("Grid passed is not the same size as the mask");
			return 0;
		}
		
		int output = 0;
		int discardedPixels = 0;
		for(int x = 0; x < maskSize; x++){
			for(int y = 0; y < maskSize; y++){
				if(originalImage[x][y] != -1){
					output += (originalImage[x][y] * mask[x][y]);
				}else {
					discardedPixels += mask[x][y];
				}
			}
		}
		if(maskAverage) {
			output = (int) (output / (maskTotal-discardedPixels));
		}
		if(output <= 0) {
			output = 0;
		}
		if(output >= 255) {
			output = 255;
		}
		return output;
	}
}
