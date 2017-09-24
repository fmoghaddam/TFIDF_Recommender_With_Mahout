package com.rekloud.recommender.controller;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;

public class Main {
	private static Logger LOG = Logger.getLogger(Main.class.getCanonicalName());
	
	public static void main(String[] args) {
		try {
			printBanner();
			final Runner run = new Runner();
			run.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (TasteException e) {
			e.printStackTrace();
		}
	}

	private static void printBanner() {
        final StringBuffer strb = new StringBuffer();
        strb.append("Start \n\n");
        strb.append(">-----------------------------------<\n");
        strb.append("> TF-IDF RecoomdationEngine         <\n");
        strb.append("> ==================                <\n");
        strb.append("> Copyright (c) 2017 by rekloud     <\n");
        strb.append("> All rights reserved.              <\n");
        strb.append("> http://www.rekloud.com/           <\n");
        strb.append("> info@rekloud.com                  <\n");
        strb.append(">-----------------------------------<\n");
        LOG.info(strb);
    }    
}
