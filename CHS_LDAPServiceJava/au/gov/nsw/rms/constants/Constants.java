package au.gov.nsw.rms.constants;


public class Constants {
	public static final String READ = "READ";
	public static final String CREATE = "CREATE";
	public static final String UPDATE = "UPDATE";
	public static final String DELETE = "DELETE";
	
	public static class HTTP{
		public static final int RESOURCE_NOT_FOUND = 404;
		public static final int INTERNAL_SERVER_ERROR = 500;
		public static final int RESOURCE_CREATED = 201;
		public static final int BAD_REQUEST = 400;
		public static final int RESOURCE_CONFLICT = 409;
		public static final int RESOURCE_UPDATED = 204;
		public static final int RESOURCE_DELETED = 204;
	}
}
