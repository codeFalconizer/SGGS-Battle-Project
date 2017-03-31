package com.trio.sos.repo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContacts {
    private static final String SHARED_PREFERENCE_CONTACTS = "EMERGENCY_CONTACTS";
    private static final String PREFERENECE_KEY_CONTACT_NUMBER = "PERSON_CONTACT_NUMBER";
    private static final String PREFERENECE_KEY_NAME = "PERSON_NAME";
    private static final String PREFERENECE_KEY_EMAIL = "PERSON_EMAIL";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
    private int maxPersons = 2;
    private List<Person> people;

    public static class Person {
        private String name;
        private String number;
        private String email;

        Person() {
        }

        public Person(String name, String number, String email) {
            this.name = name;
            this.number = number;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        public String getEmail() {
            return email;
        }

        protected void setName(String name) {
            this.name = name;
        }

        protected void setNumber(String number) {
            this.number = number;
        }

        protected void setEmail(String email) {
            this.email = email;
        }
    }

    public EmergencyContacts(Context context) {
        mContext = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_CONTACTS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        people = new ArrayList<>();
        for (int i = 0; i < maxPersons; i++) {
            Person person = new Person();
            String name = sharedPreferences.getString(PREFERENECE_KEY_NAME + i, null);
            if (name == null) {
                continue;
            }
            String number = sharedPreferences.getString(PREFERENECE_KEY_CONTACT_NUMBER + i, null);
            String email = sharedPreferences.getString(PREFERENECE_KEY_EMAIL + i, null);
            if ((number == null && email != null) || (number != null && email == null) || (number != null && email != null)) {
                person.setNumber(number);
                person.setName(name);
                person.setEmail(email);
                people.add(person);
            }
        }
    }

    public List<Person> getAllContacts() {
        return people;
    }

    public boolean addContact(String name, String phone, String email, int index) throws Exception {
        try{
            if (!(index < maxPersons) && index >= 0) {
                return false;
            } else if (name == null || name.equals("")) {
                throw new Exception("Name cannot be left blank");
            } else if ((phone == null && email == null)
                    || (phone == null && email.equals("") )
                    || (phone.equals("") && email == null)
                    || (phone.equals("") && email.equals(""))) {
                throw new Exception("Email and Phone No. both cannot be left blank");
            }
            people.get(index).setName(name);
            people.get(index).setEmail(email);
            people.get(index).setNumber(phone);
            save();
        }catch (NullPointerException e){
            return false;
        }
        return true;
    }

    public void save() {
        for (int index = 0; index < maxPersons; index++) {
            editor.putString(PREFERENECE_KEY_NAME+index, people.get(index).getName());
            editor.putString(PREFERENECE_KEY_CONTACT_NUMBER+index, people.get(index).getNumber());
            editor.putString(PREFERENECE_KEY_EMAIL+index, people.get(index).getEmail());
        }
        editor.apply();
    }

    public void clear(){
        editor.clear();
        editor.commit();
    }
}
