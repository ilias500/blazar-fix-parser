package net.openblazar.bfp.web.bean.parser;

import net.openblazar.bfp.core.parser.util.FixParserConstants;
import net.openblazar.bfp.data.fix.FixMessage;
import net.openblazar.bfp.data.user.UserDetails;
import net.openblazar.bfp.services.parser.ParserService;
import net.openblazar.bfp.web.bean.AbstractBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wojciech Zankowski
 */
@ManagedBean(name = "parserBean")
@ViewScoped
public class ParserBean extends AbstractBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(ParserBean.class);

    protected ParserService parserService;
    protected List<FixMessage> messages = new ArrayList<>();
    protected FixMessage selectedMessage;
    protected String input;

    @Inject
    public void setParserService(ParserService parserService) {
        this.parserService = parserService;
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
    }

    public void doParse(String input) {
        messages = new ArrayList<>(parserService.parseInput(input));
        doSaveMessages(messages);
    }

    protected void doSaveMessages(List<FixMessage> messages) {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) currentUser.getPrincipal();
            if (userDetails != null) {
                parserService.saveMessages(userDetails, messages);
            }
        }
    }

    public void doInjectSampleData() {
        input = FixParserConstants.SAMPLE_DATA;
    }

    public String getSender(FixMessage message) {
        return parserService.getSender(message);
    }

    public String getReceiver(FixMessage message) {
       return parserService.getReceiver(message);
    }

    public String getSendingTime(FixMessage message) {
        return parserService.getSendingTime(message);
    }

    public List<FixMessage> getMessages() {
        return messages;
    }

    public FixMessage getSelectedMessage() {
        return selectedMessage;
    }

    public void setSelectedMessage(FixMessage selectedMessage) {
        this.selectedMessage = selectedMessage;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

}