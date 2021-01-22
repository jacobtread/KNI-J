package me.jacobtread.kni;

import me.jacobtread.kni.data.MeetingNotice;
import me.jacobtread.kni.data.Notice;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KNI {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy"); // The date format used for requests
    private static final DocumentBuilder DOCUMENT_BUILDER; // The XML document builder
    private static final String DEFAULT_KEY = "vtku"; // The authentication key for KAMAR
    private static final String USER_AGENT = "KAMAR/ Linux/ Android/"; // The User-Agent for KAMAR
    private static boolean isDebug = false; // Whether or not to do debug logging

    static {
        try {
            // Create a default XML document builder
            DOCUMENT_BUILDER = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("KNI: Unable to create XML Document Parser", e);
        }
    }

    private final URL noticeUrl; // The url used to request notices
    private Proxy proxy = Proxy.NO_PROXY; // The proxy to use on requests defaults to NO_PROXY

    public KNI(String host) {
        this(host, true);
    }

    /**
     * @param host    The host domain of the KAMAR Portal (e.g portal.your.school.nz) or
     *                provide the full url https://portal.yours.school.nz/
     * @param isHTTPS Whether or not to use HTTPS:// with your URL (This is ignored if you provide a protocol)
     */
    public KNI(String host, boolean isHTTPS) {
        if (!(host.startsWith("http://") || host.startsWith("https://"))) {
            host = (isHTTPS ? "https://" : "http://") + host;
        }
        if (!host.endsWith("/")) {
            host += "/";
        }
        host += "api/api.php";
        try {
            this.noticeUrl = new URL(host);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Domain/URL for KAMAR", e);
        }
    }

    /**
     * Enabling this will print KAMARs responses to the console
     * along with any errors
     *
     * @param isDebug Whether or not to use debug logging
     */
    public static void setDebug(boolean isDebug) {
        KNI.isDebug = isDebug;
    }

    /**
     * Retrieve the notices from KAMAR using the current date
     * (This is automatically formatted)
     *
     * @return The notices object which contains the notices or an error (Checked via {@link Notices#isSuccess()})
     * @throws IOException  Thrown if an error occurs when making the request or reading the response
     * @throws SAXException Thrown if the XML response from KAMAR is invalid or contains errors
     */
    public Notices retrieve() throws IOException, SAXException {
        return retrieve(new Date());
    }

    /**
     * Retrieve the notices from KAMAR using a {@link Date} object
     * This is automatically formatted to the {@link KNI#DATE_FORMAT} format
     *
     * @param date The date object of which to fetch notices for
     * @return The notices object which contains the notices or an error (Checked via {@link Notices#isSuccess()})
     * @throws IOException  Thrown if an error occurs when making the request or reading the response
     * @throws SAXException Thrown if the XML response from KAMAR is invalid or contains errors
     */
    public Notices retrieve(Date date) throws IOException, SAXException {
        return retrieve(DATE_FORMAT.format(date));
    }

    /**
     * Retrieve the notices from KAMAR using a date string
     * MUST FOLLOW THE FORMAT USED BY {@link KNI#DATE_FORMAT}
     *
     * @param date The date to retrieve notices for
     * @return The notices object which contains the notices or an error (Checked via {@link Notices#isSuccess()})
     * @throws IOException  Thrown if an error occurs when making the request or reading the response
     * @throws SAXException Thrown if the XML response from KAMAR is invalid or contains errors
     */
    public Notices retrieve(String date) throws IOException, SAXException {
        Notices notices = new Notices(date);
        HttpURLConnection connection = (HttpURLConnection) noticeUrl.openConnection(proxy);
        // Change the User-Agent header so KAMAR will allow this request
        connection.addRequestProperty("User-Agent", USER_AGENT);
        // Specify the type of content we are sending
        connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // Tell the connection to provide us an InputStream for reading the response
        connection.setDoInput(true);
        // Tell the connection to provide us an OutputStream for writing our content
        connection.setDoOutput(true);
        // Store the output stream so we can close it later
        OutputStream outputStream = null;
        // Store the input stream so we can close it later
        InputStream inputStream = null;
        try {

            // The output stream to send data
            outputStream = connection.getOutputStream();
            // Create the request request
            String request = String.format(
                    "Key=%s&Command=GetNotices&ShowAll=YES&Date=%s",
                    URLEncoder.encode(DEFAULT_KEY, StandardCharsets.UTF_8),
                    URLEncoder.encode(date, StandardCharsets.UTF_8)
            );
            // Convert the request into bytes so it can be written to the output stream
            byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
            // Write the request bytes
            outputStream.write(requestBytes);
            // Flush the output stream
            outputStream.flush();

            // The response input stream
            inputStream = connection.getInputStream();
            // Debug logging takes a copy of the response and prints it to the console;
            if (isDebug) {
                // A StringBuilder for storing the content
                StringBuilder content = new StringBuilder();
                // Read all the lines
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                    // Stores the current line
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        // Remove any leading/trailing whitespace from the line
                        line = line.trim();
                        // Ignore empty lines
                        if (line.isEmpty()) {
                            continue;
                        }
                        // Append the line and a new line to the content StringBuilder
                        content.append(line).append('\n');
                    }
                }
                // Get rid of the last \n for formatting reasons
                content.deleteCharAt(content.length() - 1);
                // Convert the string builder into a string
                String contentRaw = content.toString();
                // Fancy printing and the response message
                System.out.println(" === START KAMAR RESPONSE ==== ");
                System.out.println(contentRaw);
                System.out.println(" ===  END KAMAR RESPONSE  ==== ");
                // Replace the empty input stream with a new stream from our content
                inputStream = new ByteArrayInputStream(contentRaw.getBytes(StandardCharsets.UTF_8));
            }
            // Create an XML document builder;
            Document document = DOCUMENT_BUILDER.parse(inputStream);

            // Find any nodes named "Error" (They will only appear if an error occurred)
            NodeList errorNodeList = document.getElementsByTagName("Error");
            // Check if any nodes are in the list
            if (errorNodeList.getLength() > 0) {
                // Get the first node (There should only be one)
                Node errorNode = errorNodeList.item(0);
                // Print the error if debug is enabled
                if (isDebug) System.out.println("ERR: Error retrieving notices: " + errorNode.getTextContent());
                // Set the error message in the notices object
                notices.setErrorMessage(errorNode.getTextContent());
            } else {
                List<Notice> noticesList = new ArrayList<>();
                // Get all nodes named "General"
                NodeList generalNodeList = document.getElementsByTagName("General");
                // Get all nodes named "Meeting"
                NodeList meetingNodeList = document.getElementsByTagName("Meeting");
                // Get the total number of nodes
                int total = generalNodeList.getLength() + meetingNodeList.getLength();
                if (total > 0) {
                    // Loop through all the indexes
                    for (int i = 0; i < total; i++) {
                        // Determine which node list to use
                        NodeList nodeList = i >= generalNodeList.getLength() ? meetingNodeList : generalNodeList;
                        // Parse the notice from the node
                        Notice notice = parseNotice(nodeList.item(i));
                        // Ignore invalid / null notices
                        if (notice != null) {
                            // Add the notice to the list
                            noticesList.add(notice);
                        }
                    }
                }
                // Store the list of notices
                notices.setNotices(noticesList);
            }
        } finally {
            // Close the input & output streams
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
        return notices;
    }

    /**
     * Parses a notice object from a XML node will return null on failure
     *
     * @param node The XML node to read
     * @return Null if the xml was invalid or the notice object
     */
    private Notice parseNotice(Node node) {
        if (node == null) {
            return null;
        }
        String nodeName = node.getNodeName();
        if(nodeName == null) {
            return null;
        }
        boolean isMeeting = nodeName.equals("Meeting");
        int index = -1;
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null && attributes.getLength() > 0) {
            Node indexNode = attributes.getNamedItem("index");
            if (indexNode != null) {
                try {
                    index = Integer.parseInt(indexNode.getTextContent());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // General Notices
        Notice.Level level = null;
        String subject = null;
        String body = null;
        String teacher = null;

        // Meeting Notices
        String place = null;
        String date = null;
        String time = null;

        // Get the XML node children
        NodeList children = node.getChildNodes();
        // Store the current index
        int nodeIndex = 0;
        // Loop through all the children
        while (nodeIndex < children.getLength()) {
            // The child XML node
            Node childNode = children.item(nodeIndex);
            // The XML node name
            String name = childNode.getNodeName();
            // The text content within the node
            String content = childNode.getTextContent();
            // Decide what to do based on the element name
            switch (name) {
                case "Level":
                    // Parse the notice level
                    level = Notice.Level.fromString(content);
                    break;
                case "Subject":
                    subject = content;
                    break;
                case "Body":
                    body = content;
                    break;
                case "Teacher":
                    teacher = content;
                    break;
                case "PlaceMeet":
                    place = content;
                    break;
                case "DateMeet":
                    date = content;
                    break;
                case "TimeMeet":
                    time = content;
                    break;
            }
            // Increase the index
            nodeIndex++;
        }

        // Ignore invalid notices (bad server response?) This shouldn't happen but just in case
        if (level == null || subject == null || body == null || teacher == null) {
            return null;
        }
        // Not actually a meeting notice?
        if (isMeeting && (place == null || date == null || time == null)) {
            return null;
        }

        if (isMeeting) {
            return new MeetingNotice(index, level, subject, body, teacher, place, date, time);
        } else {
            return new Notice(index, level, subject, body, teacher);
        }
    }

    /**
     * Closes a object while ignoring any nulls and exceptions (quietly shhhh)
     *
     * @param closeable The closeable object
     */
    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Set a proxy for the connection to use
     *
     * @param proxy The proxy to use
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

}
