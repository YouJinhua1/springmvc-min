package com.yjh.view;

public class ViewResolver {
	
	private String prefix;
    private String suffix;
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getSuffix() {
        return suffix;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String jspMapping(String value){
        return this.prefix+value+this.suffix;
    }
}
