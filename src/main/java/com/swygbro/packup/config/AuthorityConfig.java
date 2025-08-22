package com.swygbro.packup.config;

import org.springframework.security.core.GrantedAuthority;

public enum AuthorityConfig implements GrantedAuthority{
	

	// 권한
	ADMIN(ROLES.ADMIN, "통합관리자", "ADMIN"),
	USER(ROLES.USER, "서비스관리자", "USER"),
	CHANGE(ROLES.CHANGE, "권한변경", "CHANGE");
	
	// 권한 실제 명칭
	public static class ROLES{
		public static final String ADMIN = "ROLE_ADMIN";					// 통합관리자
		public static final String USER = "ROLE_USER";       				// 서비스관리자
		public static final String CHANGE = "ROLE_CHANGE";       				// 권한변경시
	}
	
	private String authority;
	private String description;
	private String authNm;
	
	private AuthorityConfig(String authority, String description, String authNm) {
		this.authority = authority;
		this.description = description;
		this.authNm = authNm;
	}
	
	// 권한 실제 명칭
	@Override
	public String getAuthority() {
		return authority;
	}
	
	// 권한 한글 설명
	public String getDescription() {
		return description;
		
	}
	
	public String getAuthNm() {
		return authNm;
	}


}

	
