//
//  MNWSInfoRequest.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;

public abstract class MNWSInfoRequest
 {
  public static class RequestResult
   {
    public boolean hadError ()
     {
      return failed;
     }

    public String getErrorMessage ()
     {
      return errorMessage;
     }

    protected RequestResult ()
     {
      failed       = false;
      errorMessage = null;
     }

    protected void setError (String errorMessage)
     {
      failed            = true;
      this.errorMessage = errorMessage;
     }

    private boolean failed;
    private String  errorMessage;
   }

  abstract void addContent             (MNWSRequestContent content);
  abstract void handleRequestCompleted (MNWSResponse       response);
  abstract void handleRequestError     (MNWSRequestError   error);
 }

