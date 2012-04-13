//
//  MNDirectPopup.java
//  MultiNet client
//
//  Copyright 2010 PlayPhone. All rights reserved.
//

package com.playphone.multinet;

public class MNDirectPopup {
	public final static int MNDIRECTPOPUP_WELCOME              = 1 << 0;
	public final static int MNDIRECTPOPUP_ACHIEVEMENTS         = 1 << 1;
	public final static int MNDIRECTPOPUP_NEW_HI_SCORES        = 1 << 2;
	public final static int MNDIRECTPOPUP_WEB_EVENT            = 1 << 3; // Unable turn off
	@Deprecated public final static int MNDIRECTPOPUP_OLD_SHOW_MODE        = 1 << 31;
	public final static int MNDIRECTPOPUP_ALL = MNDIRECTPOPUP_WELCOME
			| MNDIRECTPOPUP_ACHIEVEMENTS | MNDIRECTPOPUP_NEW_HI_SCORES | MNDIRECTPOPUP_WEB_EVENT;	

	protected static boolean isActiveFlag = false;
	protected static int actionsBitMask = 0;
	
	public static void init (int actionsBitMask) {
		MNDirectPopup.actionsBitMask = actionsBitMask;
		setActive(true);
	}
	
	public static boolean isOldShowMode() {
		return ((MNDIRECTPOPUP_OLD_SHOW_MODE & actionsBitMask) != 0);
	}
	
	public static synchronized boolean isActive () {
		return isActiveFlag;
	}
	
	static MNInfoPanelAchievement achievementPanel = null;
	static MNInfoPanelNetwork     networkPanel = null;
	static MNInfoPanelHighScore   highScorePanel = null;
	static MNInfoPanelWebEvent    eventPanel = null;
	
	public static synchronized void setActive (boolean activeFlag) {
		if (isActiveFlag != activeFlag) {
			if (activeFlag) {
				if ((actionsBitMask & MNDIRECTPOPUP_WELCOME) > 0) {
					if (networkPanel == null) {
						networkPanel = new MNInfoPanelNetwork(); 
					}
					MNDirectUIHelper.addEventHandler(networkPanel.getDirectUIEventHandler ());
				}

				if ((actionsBitMask & MNDIRECTPOPUP_ACHIEVEMENTS) > 0) {
					if (achievementPanel == null) {
						achievementPanel = new MNInfoPanelAchievement();
					}
					MNDirectUIHelper.addEventHandler(achievementPanel.getDirectUIEventHandler());
				}
				
				if ((actionsBitMask & MNDIRECTPOPUP_NEW_HI_SCORES) > 0) {
					if (highScorePanel == null) {
						highScorePanel = new MNInfoPanelHighScore();
					}
					MNDirectUIHelper.addEventHandler(highScorePanel.getDirectUIEventHandler());
				}
				
				// Always be enabled
				// if ((actionsBitMask & MNDIRECTPOPUP_WEB_EVENT) > 0) {
					if (eventPanel == null) {
						eventPanel = new MNInfoPanelWebEvent();
					}
					MNDirectUIHelper.addEventHandler(eventPanel.getDirectUIEventHandler());
				// }
			} else {
				if (networkPanel != null) MNDirectUIHelper.removeEventHandler(networkPanel.getDirectUIEventHandler ());
				if (achievementPanel != null) MNDirectUIHelper.removeEventHandler(achievementPanel.getDirectUIEventHandler());
				if (highScorePanel != null) MNDirectUIHelper.removeEventHandler(highScorePanel.getDirectUIEventHandler());
				if (eventPanel != null) MNDirectUIHelper.removeEventHandler(eventPanel.getDirectUIEventHandler());
			}
			
			isActiveFlag = activeFlag;
		}
	}
}
