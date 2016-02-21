package net.openblazar.bfp.services.parser;

import com.google.inject.Inject;
import net.openblazar.bfp.core.parser.FixParser;
import net.openblazar.bfp.data.fix.FixMessage;
import net.openblazar.bfp.database.dao.MessageDAO;

import java.util.List;

/**
 * @author Wojciech Zankowski
 */
public class ParserServiceImpl implements ParserService {

    private final FixParser fixParser;
    private MessageDAO messageDAO;

    @Inject
    public ParserServiceImpl(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
        this.fixParser = new FixParser();
    }

    @Override
    public List<FixMessage> parseInput(String input) {
        return fixParser.parseInput(input);
    }

    @Override
    public void addMessages(List<FixMessage> messages) {
        for (FixMessage message : messages) {
            try {
                messageDAO.saveMessage(message);
            } catch (Exception e) {
                // TODO
            }
        }
    }

}