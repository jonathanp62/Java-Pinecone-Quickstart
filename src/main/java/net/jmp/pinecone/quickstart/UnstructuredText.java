package net.jmp.pinecone.quickstart;

/*
 * (#)UnstructuredText.java 0.1.0   05/18/2025
 *
 * @author   Jonathan Parker
 *
 * MIT License
 *
 * Copyright (c) 2025 Jonathan M. Parker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.ArrayList;
import java.util.List;

/// The quickstart class.
///
/// @version    0.1.0
/// @since      0.1.0
final class UnstructuredText {
    /// The text list.
    private final List<Text> textList = new ArrayList<>(50);

    /// The default constructor.
    UnstructuredText() {
        super();

        this.loadTextList();
    }

    /// Load the text list.
    private void loadTextList() {
        this.textList.add(new Text("rec1", "The Eiffel Tower was completed in 1889 and stands in Paris, France.", "history"));
        this.textList.add(new Text("rec2", "Photosynthesis allows plants to convert sunlight into energy.", "science"));
        this.textList.add(new Text("rec3", "Albert Einstein developed the theory of relativity.", "science"));
        this.textList.add(new Text("rec4", "The mitochondrion is often called the powerhouse of the cell.", "biology"));
        this.textList.add(new Text("rec5", "Shakespeare wrote many famous plays, including Hamlet and Macbeth.", "literature"));
        this.textList.add(new Text("rec6", "Water boils at 100°C under standard atmospheric pressure.", "physics"));
        this.textList.add(new Text("rec7", "The Great Wall of China was built to protect against invasions.", "history"));
        this.textList.add(new Text("rec8", "Honey never spoils due to its low moisture content and acidity.", "food science"));
        this.textList.add(new Text("rec9", "The speed of light in a vacuum is approximately 299,792 km/s.", "physics"));
        this.textList.add(new Text("rec10", "Newton’s laws describe the motion of objects.", "physics"));
        this.textList.add(new Text("rec11", "The human brain has approximately 86 billion neurons.", "biology"));
        this.textList.add(new Text("rec12", "The Amazon Rainforest is one of the most biodiverse places on Earth.", "geography"));
        this.textList.add(new Text("rec13", "Black holes have gravitational fields so strong that not even light can escape.", "astronomy"));
        this.textList.add(new Text("rec14", "The periodic table organizes elements based on their atomic number.", "chemistry"));
        this.textList.add(new Text("rec15", "Leonardo da Vinci painted the Mona Lisa.", "art"));
        this.textList.add(new Text("rec16", "The internet revolutionized communication and information sharing.", "technology"));
        this.textList.add(new Text("rec17", "The Pyramids of Giza are among the Seven Wonders of the Ancient World.", "history"));
        this.textList.add(new Text("rec18", "Dogs have an incredible sense of smell, much stronger than humans.", "biology"));
        this.textList.add(new Text("rec19", "The Pacific Ocean is the largest and deepest ocean on Earth.", "geography"));
        this.textList.add(new Text("rec20", "Chess is a strategic game that originated in India.", "games"));
        this.textList.add(new Text("rec21", "The Statue of Liberty was a gift from France to the United States.", "history"));
        this.textList.add(new Text("rec22", "Coffee contains caffeine, a natural stimulant.", "food science"));
        this.textList.add(new Text("rec23", "Thomas Edison invented the practical electric light bulb.", "inventions"));
        this.textList.add(new Text("rec24", "The moon influences ocean tides due to gravitational pull.", "astronomy"));
        this.textList.add(new Text("rec25", "DNA carries genetic information for all living organisms.", "biology"));
        this.textList.add(new Text("rec26", "Rome was once the center of a vast empire.", "history"));
        this.textList.add(new Text("rec27", "The Wright brothers pioneered human flight in 1903.", "inventions"));
        this.textList.add(new Text("rec28", "Bananas are a good source of potassium.", "nutrition"));
        this.textList.add(new Text("rec29", "The stock market fluctuates based on supply and demand.", "economics"));
        this.textList.add(new Text("rec30", "A compass needle points toward the magnetic north pole.", "navigation"));
        this.textList.add(new Text("rec31", "The universe is expanding, according to the Big Bang theory.", "astronomy"));
        this.textList.add(new Text("rec32", "Elephants have excellent memory and strong social bonds.", "biology"));
        this.textList.add(new Text("rec33", "The violin is a string instrument commonly used in orchestras.", "music"));
        this.textList.add(new Text("rec34", "The heart pumps blood throughout the human body.", "biology"));
        this.textList.add(new Text("rec35", "Ice cream melts when exposed to heat.", "food science"));
        this.textList.add(new Text("rec36", "Solar panels convert sunlight into electricity.", "technology"));
        this.textList.add(new Text("rec37", "The French Revolution began in 1789.", "history"));
        this.textList.add(new Text("rec38", "The Taj Mahal is a mausoleum built by Emperor Shah Jahan.", "history"));
        this.textList.add(new Text("rec39", "Rainbows are caused by light refracting through water droplets.", "physics"));
        this.textList.add(new Text("rec40", "Mount Everest is the tallest mountain in the world.", "geography"));
        this.textList.add(new Text("rec41", "Octopuses are highly intelligent marine creatures.", "biology"));
        this.textList.add(new Text("rec42", "The speed of sound is around 343 meters per second in air.", "physics"));
        this.textList.add(new Text("rec43", "Gravity keeps planets in orbit around the sun.", "astronomy"));
        this.textList.add(new Text("rec44", "The Mediterranean diet is considered one of the healthiest in the world.", "nutrition"));
        this.textList.add(new Text("rec45", "A haiku is a traditional Japanese poem with a 5-7-5 syllable structure.", "literature"));
        this.textList.add(new Text("rec46", "The human body is made up of about 60% water.", "biology"));
        this.textList.add(new Text("rec47", "The Industrial Revolution transformed manufacturing and transportation.", "history"));
        this.textList.add(new Text("rec48", "Vincent van Gogh painted Starry Night.", "art"));
        this.textList.add(new Text("rec49", "Airplanes fly due to the principles of lift and aerodynamics.", "physics"));
        this.textList.add(new Text("rec50", "Renewable energy sources include wind, solar, and hydroelectric power.", "energy"));
    }

    /// Get the text list.
    ///
    /// @return     java.util.List<net.jmp.pinecone.quickstart.UnstructuredText.Text>
    List<Text> getTextList() {
        return this.textList;
    }

    /// The text class.
    static class Text {
        /// The text identifier.
        private final String id;

        /// The text.
        private final String text;

        /// The category.
        private final String category;

        /// The constructor.
        ///
        /// @param  id          java.lang.String
        /// @param  text        java.lang.String
        /// @param  category    java.lang.String
        public Text(final String id, final String text, final String category) {
            super();

            this.id = id;
            this.text = text;
            this.category = category;
        }

        /// Get the text identifier.
        ///
        /// @return     java.lang.String
        public String getId() {
            return this.id;
        }

        /// Get the text.
        ///
        /// @return     java.lang.String
        public String getText() {
            return this.text;
        }

        /// Get the category.
        ///
        /// @return     java.lang.String
        public String getCategory() {
            return this.category;
        }
    }
}
