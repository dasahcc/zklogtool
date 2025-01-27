/* 
 * Copyright 2014 Alen Caljkusic.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zklogtool.web.components;

import org.apache.zookeeper.ZooDefs.OpCode;

public enum ZkOperations {
	
	CREATE("create"),
	DELTE("delete"),
	SET_DATA("set data"),
	SET_ACL("set ACL"),
	CHECK("check"),
	MULTI("multi"),
	CREATE_SESSION("create session"),
	CLOSE_SESSION("close session"),
	ERROR("error");

    private final String name;       

    private ZkOperations(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
       return name;
    }

	

}
