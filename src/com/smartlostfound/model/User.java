package com.smartlostfound.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a person reporting, owning, or claiming an item in the system.
 * Encapsulates personal identity and communication details with validation.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String email;
    private String phone;

    /**
     * Default constructor for serialization support.
     */
    public User() {
        this("", "", "");
    }

    /**
     * Constructs a User with full contact information.
     *
     * @param name Full name of the user
     * @param email Contact email address
     * @param phone Contact phone number
     */
    public User(String name, String email, String phone) {
        setName(name);
        setEmail(email);
        setPhone(phone);
    }

    /**
     * Convenient constructor when only name is known.
     *
     * @param name Full name of the user
     */
    public User(String name) {
        this(name, "", "");
    }

    /**
     * Convenient constructor when only name and primary contact are known.
     *
     * @param name Full name of the user
     * @param email Contact email address
     */
    public User(String name, String email) {
        this(name, email, "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name == null) ? "" : name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = (email == null) ? "" : email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = (phone == null) ? "" : phone.trim();
    }

    /**
     * Checks if this user has at least a valid name and one contact method.
     *
     * @return True if user data meets minimum reporting requirements
     */
    public boolean isValid() {
        return !name.isEmpty() && (!email.isEmpty() || !phone.isEmpty());
    }

    /**
     * Returns a consolidated contact string combining email and phone.
     *
     * @return Formatted contact string
     */
    public String getContactDetails() {
        StringBuilder sb = new StringBuilder();
        if (!email.isEmpty()) {
            sb.append(email);
        }
        if (!phone.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(phone);
        }
        return sb.length() > 0 ? sb.toString() : "No contact provided";
    }

    @Override
    public String toString() {
        return name + " (" + getContactDetails() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) &&
               Objects.equals(email, user.email) &&
               Objects.equals(phone, user.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, phone);
    }
}
