package model;

public abstract class Scrape {
	
	public Scrape() {
		
	}
	
	public abstract String placeInString(String str, String addition);
	
	public static final Scrape DOCUMENT = new Scrape() {

		@Override
		public String placeInString(String str, String addition) {
			return "document";
		}
		
	};
	
	public static final Scrape TAG = new Scrape() {
		@Override
		public String placeInString(String str, String addition) {
			return str + ".querySelector('"+addition+"')";
		}
	};
	
}
