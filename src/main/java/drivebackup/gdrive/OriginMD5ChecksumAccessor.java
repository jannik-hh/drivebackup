package drivebackup.gdrive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Property;

public class OriginMD5ChecksumAccessor {
	public final static String MD5_PROPERTY_NAME = "orig_md5";

	private final File file;
	
	public OriginMD5ChecksumAccessor(File file){
		this.file = file;
	}
	
	public Optional<String> get(){
		return getMD5Property().map((prop) -> prop.getValue());
	}
	
	public void set(String originMD5Checksum) throws IOException {		
		Optional<Property> optMd5Property = getMD5Property();
		if(optMd5Property.isPresent()){
			optMd5Property.get().setValue(originMD5Checksum);
		}else{
			Property property = new Property();
			property.setKey(MD5_PROPERTY_NAME).setValue(originMD5Checksum).setVisibility("PUBLIC");
			List<Property> properties = getProperties();
			properties.add(property);
		}
	}
	
	private Optional<Property> getMD5Property(){
		return getProperties().stream()
				.filter((property) -> property.getKey().equals(MD5_PROPERTY_NAME)).findFirst();
	}
	
	private List<Property> getProperties(){
		List<Property> properties = file.getProperties();
		if(properties == null){
			properties = new ArrayList<Property>();
			file.setProperties(properties);
		}
		return properties;
	}
}
