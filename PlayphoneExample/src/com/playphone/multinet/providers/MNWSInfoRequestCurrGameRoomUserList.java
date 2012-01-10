//
//  MNWSInfoRequestCurrGameRoomUserList.java
//  MultiNet client
//
//  Copyright 2012 PlayPhone. All rights reserved.
//

package com.playphone.multinet.providers;

import java.util.ArrayList;

import com.playphone.multinet.core.ws.MNWSRequestContent;
import com.playphone.multinet.core.ws.MNWSResponse;
import com.playphone.multinet.core.ws.MNWSRequestError;
import com.playphone.multinet.core.ws.data.MNWSRoomUserInfoItem;

public class MNWSInfoRequestCurrGameRoomUserList extends MNWSInfoRequest
 {
  public MNWSInfoRequestCurrGameRoomUserList (int roomSFId, IEventHandler eventHandler)
   {
    this.roomSFId     = roomSFId;
    this.eventHandler = eventHandler;
   }

  /* package */ void addContent (MNWSRequestContent content)
   {
    blockName = content.addCurrGameRoomUserList(roomSFId);
   }

  @SuppressWarnings("unchecked")
  void handleRequestCompleted (MNWSResponse       response)
   {
    ArrayList<MNWSRoomUserInfoItem> data = (ArrayList<MNWSRoomUserInfoItem>)response.getDataForBlock(blockName);

    RequestResult result = new RequestResult(data != null ?
                                             data.toArray(new MNWSRoomUserInfoItem[data.size()]) :
                                             new MNWSRoomUserInfoItem[0]);

    eventHandler.onCompleted(result);
   }

  void handleRequestError (MNWSRequestError   error)
   {
    RequestResult result = new RequestResult(null);

    result.setError(error.getMessage());

    eventHandler.onCompleted(result);
   }

  public interface IEventHandler
   {
    public void onCompleted (RequestResult result);
   }

  public static class EventHandlerAbstract implements IEventHandler
   {
    public void onCompleted (RequestResult result)
     {
     }
   }

  public static class RequestResult extends MNWSInfoRequest.RequestResult
   {
    public MNWSRoomUserInfoItem[] getDataEntry ()
     {
      return data;
     }

    private RequestResult (MNWSRoomUserInfoItem[] data)
     {
      super();

      this.data = data;
     }

    private MNWSRoomUserInfoItem[] data;
   }

  private final int           roomSFId;
  private final IEventHandler eventHandler;
  private String              blockName;
 }

