package data;


public interface IStorable{
	static final int notAnId = 0;
	
	int getUniqueId();
	String getShortName();
}
