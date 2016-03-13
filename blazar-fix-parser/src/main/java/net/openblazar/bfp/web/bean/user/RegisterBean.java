package net.openblazar.bfp.web.bean.user;

import net.openblazar.bfp.services.user.UserService;
import net.openblazar.bfp.web.bean.AbstractBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 * @author Wojciech Zankowski
 */
@ManagedBean(name = "registerBean")
@RequestScoped
public class RegisterBean extends AbstractBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(RegisterBean.class);

    private UserService userService;

    private String username;
    private String email;
    private String password;
    private String passwordConfirm;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public void doRegister() {
        if (!getPassword().equals(getPasswordConfirm())) {
            facesError("Password mismatch.", new Exception(""));
            return;
        }

        try {
            if (userService.isUserExists(getUsername())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "User with given name " + getUsername() + " already exists.", ""));
                return;
            }
            userService.registerUser(getUsername(), "", getPassword().toCharArray());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Congratulations, registration has been successful", ""));
        } catch (Exception e) {
            facesError("Failed to register user.", e);
        }
    }

    protected void facesError(String message, Exception exception) {
        super.facesError(message, exception);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, exception.getMessage(), null));
        LOGGER.error(message, exception);
    }

}