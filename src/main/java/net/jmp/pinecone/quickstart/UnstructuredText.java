package net.jmp.pinecone.quickstart;

/*
 * (#)UnstructuredText.java 0.2.0   05/21/2025
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

import java.util.LinkedHashMap;
import java.util.Map;

/// The quickstart class.
///
/// @version    0.2.0
/// @since      0.1.0
final class UnstructuredText {
    /// The text map. Use a linked hash map to preserve insertion order.
    private final Map<String, Text> textMap = new LinkedHashMap<>(50);

    /// The default constructor.
    UnstructuredText() {
        super();

        this.loadTextMap();
    }

    /// Load the text map.
    private void loadTextMap() {
        this.textMap.put("rec1", new Text("The Eiffel Tower was completed in 1889 and stands in Paris, France.", "history"));
        this.textMap.put("rec2", new Text("Photosynthesis allows plants to convert sunlight into energy.", "science"));
        this.textMap.put("rec3", new Text("Albert Einstein developed the theory of relativity.", "science"));
        this.textMap.put("rec4", new Text("The mitochondrion is often called the powerhouse of the cell.", "biology"));
        this.textMap.put("rec5", new Text("Shakespeare wrote many famous plays, including Hamlet and Macbeth.", "literature"));
        this.textMap.put("rec6", new Text("Water boils at 100°C under standard atmospheric pressure.", "physics"));
        this.textMap.put("rec7", new Text("The Great Wall of China was built to protect against invasions.", "history"));
        this.textMap.put("rec8", new Text("Honey never spoils due to its low moisture content and acidity.", "food science"));
        this.textMap.put("rec9", new Text("The speed of light in a vacuum is approximately 299,792 km/s.", "physics"));
        this.textMap.put("rec10", new Text("Newton’s laws describe the motion of objects.", "physics"));
        this.textMap.put("rec11", new Text("The human brain has approximately 86 billion neurons.", "biology"));
        this.textMap.put("rec12", new Text("The Amazon Rainforest is one of the most biodiverse places on Earth.", "geography"));
        this.textMap.put("rec13", new Text("Black holes have gravitational fields so strong that not even light can escape.", "astronomy"));
        this.textMap.put("rec14", new Text("The periodic table organizes elements based on their atomic number.", "chemistry"));
        this.textMap.put("rec15", new Text("Leonardo da Vinci painted the Mona Lisa.", "art"));
        this.textMap.put("rec16", new Text("The internet revolutionized communication and information sharing.", "technology"));
        this.textMap.put("rec17", new Text("The Pyramids of Giza are among the Seven Wonders of the Ancient World.", "history"));
        this.textMap.put("rec18", new Text("Dogs have an incredible sense of smell, much stronger than humans.", "biology"));
        this.textMap.put("rec19", new Text("The Pacific Ocean is the largest and deepest ocean on Earth.", "geography"));
        this.textMap.put("rec20", new Text("Chess is a strategic game that originated in India.", "games"));
        this.textMap.put("rec21", new Text("The Statue of Liberty was a gift from France to the United States.", "history"));
        this.textMap.put("rec22", new Text("Coffee contains caffeine, a natural stimulant.", "food science"));
        this.textMap.put("rec23", new Text("Thomas Edison invented the practical electric light bulb.", "inventions"));
        this.textMap.put("rec24", new Text("The moon influences ocean tides due to gravitational pull.", "astronomy"));
        this.textMap.put("rec25", new Text("DNA carries genetic information for all living organisms.", "biology"));
        this.textMap.put("rec26", new Text("Rome was once the center of a vast empire.", "history"));
        this.textMap.put("rec27", new Text("The Wright brothers pioneered human flight in 1903.", "inventions"));
        this.textMap.put("rec28", new Text("Bananas are a good source of potassium.", "nutrition"));
        this.textMap.put("rec29", new Text("The stock market fluctuates based on supply and demand.", "economics"));
        this.textMap.put("rec30", new Text("A compass needle points toward the magnetic north pole.", "navigation"));
        this.textMap.put("rec31", new Text("The universe is expanding, according to the Big Bang theory.", "astronomy"));
        this.textMap.put("rec32", new Text("Elephants have excellent memory and strong social bonds.", "biology"));
        this.textMap.put("rec33", new Text("The violin is a string instrument commonly used in orchestras.", "music"));
        this.textMap.put("rec34", new Text("The heart pumps blood throughout the human body.", "biology"));
        this.textMap.put("rec35", new Text("Ice cream melts when exposed to heat.", "food science"));
        this.textMap.put("rec36", new Text("Solar panels convert sunlight into electricity.", "technology"));
        this.textMap.put("rec37", new Text("The French Revolution began in 1789.", "history"));
        this.textMap.put("rec38", new Text("The Taj Mahal is a mausoleum built by Emperor Shah Jahan.", "history"));
        this.textMap.put("rec39", new Text("Rainbows are caused by light refracting through water droplets.", "physics"));
        this.textMap.put("rec40", new Text("Mount Everest is the tallest mountain in the world.", "geography"));
        this.textMap.put("rec41", new Text("Octopuses are highly intelligent marine creatures.", "biology"));
        this.textMap.put("rec42", new Text("The speed of sound is around 343 meters per second in air.", "physics"));
        this.textMap.put("rec43", new Text("Gravity keeps planets in orbit around the sun.", "astronomy"));
        this.textMap.put("rec44", new Text("The Mediterranean diet is considered one of the healthiest in the world.", "nutrition"));
        this.textMap.put("rec45", new Text("A haiku is a traditional Japanese poem with a 5-7-5 syllable structure.", "literature"));
        this.textMap.put("rec46", new Text("The human body is made up of about 60% water.", "biology"));
        this.textMap.put("rec47", new Text("The Industrial Revolution transformed manufacturing and transportation.", "history"));
        this.textMap.put("rec48", new Text("Vincent van Gogh painted Starry Night.", "art"));
        this.textMap.put("rec49", new Text("Airplanes fly due to the principles of lift and aerodynamics.", "physics"));
        this.textMap.put("rec50", new Text("Renewable energy sources include wind, solar, and hydroelectric power.", "energy"));
    }

    /// Get the text map.
    ///
    /// @return     java.util.Map<java.lang.String, net.jmp.pinecone.quickstart.UnstructuredText.Text>
    Map<String, Text> getTextMap() {
        return this.textMap;
    }

    /// Get the text by key.
    ///
    /// @param  key     java.lang.String
    /// @return         net.jmp.pinecone.quickstart.UnstructuredText.Text
    /// @since          0.2.0
    Text lookup(final String key) {
        return this.textMap.get(key);
    }

    /// The text class.
    static class Text {
        /// The text.
        private final String content;

        /// The category.
        private final String category;

        /// The constructor.
        ///
        /// @param  content     java.lang.String
        /// @param  category    java.lang.String
        public Text(final String content, final String category) {
            super();

            this.content = content;
            this.category = category;
        }

        /// Get the content.
        ///
        /// @return     java.lang.String
        public String getContent() {
            return this.content;
        }

        /// Get the category.
        ///
        /// @return     java.lang.String
        public String getCategory() {
            return this.category;
        }
    }
}
