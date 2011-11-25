//
//  MNAddrBookAccess.java
//  MultiNet client
//
//  Copyright 2010 PlayPhone. All rights reserved.
//

package com.playphone.multinet.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.util.Log;

@SuppressWarnings("deprecation")
class MNAddrBookAccess
 {
  static class AddressInfo
   {
    public AddressInfo(String name, String email)
     {
      this.name = name;
      this.email = email;
     }

    public String name;
    public String email;
   }

  protected static Collection<String> getEmail(String id, ContentResolver cr)
   {
    List<String> result = new ArrayList<String>();

    Cursor emailCur = cr.query(Contacts.ContactMethods.CONTENT_EMAIL_URI, null,
      Contacts.ContactMethods.PERSON_ID + " = ?", new String[]{id}, null);

    while (emailCur.moveToNext())
     { // This would allow you get several email
      result.add(emailCur.getString(emailCur
        .getColumnIndex(Contacts.ContactMethods.DATA)));
     }

    emailCur.close();

    return result;
   }

  /**
   * @param cr ContentResolver (simple get activity.getContentResolver()) 
   * @return address array or null on security exception
   */
  public static List<AddressInfo> getAddressBookRecords(ContentResolver cr)
   {
    List<AddressInfo> resultList = new ArrayList<AddressInfo>();
    Cursor cur = null;

    try
     {
       cur = cr.query(People.CONTENT_URI, null, null, null, null);
     }
    catch (SecurityException e)
     {
       return null;
     }    
    
    if (cur.getCount() > 0)
     {
      while (cur.moveToNext())
       {
        String id = cur.getString(cur.getColumnIndex(People._ID));
        String name = cur.getString(cur.getColumnIndex(People.DISPLAY_NAME));

        Collection<String> el = getEmail(id, cr);

        for (String email : el)
         {
          resultList.add(new AddressInfo(name, email));
         }
       }
     }

    return resultList;
   }
  
	public static class UserAddressInfo {
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getEmails() {
			return emails;
		}

		public void setEmails(List<String> emails) {
			this.emails = emails;
		}

		public List<String> getPhones() {
			return phones;
		}

		public void setPhones(List<String> phones) {
			this.phones = phones;
		}

		public String name;
		public List<String> emails;
		public List<String> phones;
	}
  
	protected interface AddressBook {
		public Collection<UserAddressInfo> getUserAddressInfo(final ContentResolver cr);
	}

	protected static class OldVersionAddressBook implements AddressBook {
		@Override
		public Collection<UserAddressInfo> getUserAddressInfo(final ContentResolver cr) {
			return new ArrayList<MNAddrBookAccess.UserAddressInfo>();
		}
	}

	protected static class CurrentVersionAddressBook implements AddressBook {

		protected List<String> getEmails(String personId, final ContentResolver cr) {
			List<String> result = new ArrayList<String>();

			Cursor cur = cr.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
					new String[] { personId }, null);

			if (cur.moveToFirst()) {
				do {
					result.add(cur.getString(cur
							.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
				} while (cur.moveToNext());
			}
			cur.close();
			return result;
		}

		protected List<String> getPhones(String personId,
				final ContentResolver cr) {
			List<String> result = new ArrayList<String>();

			Cursor cur = cr.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
					new String[] { String.valueOf(personId) }, null);
			if (cur.moveToFirst()) {
				do {
					result.add(cur.getString(cur
							.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				} while (cur.moveToNext());
			}
			cur.close();
			return result;
		}

		@Override
		public Collection<UserAddressInfo> getUserAddressInfo(
				final ContentResolver cr) {
			List<MNAddrBookAccess.UserAddressInfo> addressBook = new ArrayList<MNAddrBookAccess.UserAddressInfo>();
			try {
				final Cursor cur = cr.query(
						ContactsContract.Contacts.CONTENT_URI, null, null,
						null, null);

				if (cur != null) {
					if (cur.moveToFirst()) {
						do {
							String personId = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
							String name = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
							
							MNAddrBookAccess.UserAddressInfo addressItem = new UserAddressInfo();
							
							addressItem.setName(name);
							addressItem.setEmails(this.getEmails(personId, cr));
							addressItem.setPhones(this.getPhones(personId, cr));
							
							addressBook.add(addressItem);
							
						} while (cur.moveToNext());
					}
					cur.close();
				}

			} catch (SecurityException e) {
				Log.d(this.getClass().getSimpleName(), "SecurityException");
			}

			return addressBook;
		}
	}

	protected static AddressBook newInstance() {
		final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
			return new OldVersionAddressBook();
		} else {
			return new CurrentVersionAddressBook();
		}
	}  

	public static Collection<UserAddressInfo> getAddressBook(final ContentResolver cr) {
		return newInstance().getUserAddressInfo(cr);
	}
 }
