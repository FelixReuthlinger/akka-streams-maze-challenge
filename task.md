## The Challenge
The challenge proposed here is to build a system which acts as a socket
server, reading events from an *event source* and forwarding them when
appropriate to *user clients*.

Clients will connect through TCP and use the simple protocol described in a
section below. There will be two types of clients connecting to your server:

- **One** *event source*: It will send you a
stream of events which may or may not require clients to be notified
- **Many** *user clients*: Each one representing a specific user,
these wait for notifications for events which would be relevant to the
user they represent

### The Protocol
The protocol used by the clients is string-based (i.e. a `CRLF` control
character terminates each message). All strings are encoded in `UTF-8`.

The *event source* **connects on port 9090** and will start sending
events as soon as the connection is accepted.

The many *user clients* will **connect on port 9099**. As soon
as the connection is accepted, they will send to the server the ID of
the represented user, so that the server knows which events to
inform them of. For example, once connected a *user client* may send down:
`2932\r\n`, indicating that they are representing user 2932.

After the identification is sent, the *user client* starts waiting for
events to be sent to them. Events coming from *event source* should be
sent to relevant *user clients* exactly like read, no modification is
required or allowed.

### The Events
There are five possible events. The table below describe payloads
sent by the *event source* and what they represent:

| Payload       | Sequence #| Type         | From User Id | To User Id |
|---------------|-----------|--------------|--------------|------------|
|666\|F\|60\|50 | 666       | maze.app.model.Follow       | 60           | 50         |
|1\|U\|12\|9    | 1         | maze.app.model.Unfollow     | 12           | 9          |
|542532\|B      | 542532    | maze.app.model.Broadcast    | -            | -          |
|43\|P\|32\|56  | 43        | Private Msg  | 32           | 56         |
|634\|S\|32     | 634       | Status Update| 32           | -          |

Using the verification program supplied, you will receive exactly 10000000 events,
with sequence number from 1 to 10000000. **The events will arrive out of order**.

*Note: Please do not assume that your code would only handle a finite sequence
of events, **we expect your server to handle an arbitrarily large events stream**
(i.e. you would not be able to keep all events in memory or any other storage)*

Events may generate notifications for *user clients*. **If there is a
*user client* ** connected for them, these are the users to be
informed for different event types:

* **maze.app.model.Follow**: Only the `To User Id` should be notified
* **maze.app.model.Unfollow**: No clients should be notified
* **maze.app.model.Broadcast**: All connected *user clients* should be notified
* **Private Message**: Only the `To User Id` should be notified
* **Status Update**: All current followers of the `From User ID` should be notified

If there are no *user client* connected for a user, any notifications
for them must be silently ignored. *user clients* expect to be notified of
events **in the correct order**, regardless of the order in which the
*event source* sent them.

## Test Client

You can find the test client in the `client` folder. It will initialize both 
*event source client* on port `9090` and *user clients* on port `9099`.

To run it use:

```bash
./followermaze.sh
```

### The Configuration

During development, it is possible to modify the test program behavior using the 
following environment variables:

1. **logLevel** - Default: info

   Modify to "debug" to print debug messages.

2. **eventListenerPort** - Default: 9090

   The port used by the event source.

3. **clientListenerPort** - Default: 9099

   The port used to register clients.

4. **totalEvents** - Default: 10000000

   Number of messages to send.

5. **concurrencyLevel** - Default: 100

   Number of conected users.

6. **numberOfUsers** Default: concurrencyLevel * 10
	
   Total number of users (connected or not)

7. **randomSeed** - Default: 666
	
   The seed to generate random values

8. **timeout** - Default: 20000
	
   Timeout in milliseconds for clients while waiting for new messages

9. **maxEventSourceBatchSize** - Default: 100

   The event source flushes messages in random batch sizes and ramdomize the messages
   order for each batch. For example, if this configuration is "1" the event source 
   will send only ordered messages flushing the connection for each message.

10. **logInterval** - Default: 1000

   The interval in milliseconds used to log the sent messages counter.

## Your task
The application has to be build using one of the following languages: Java, Scala. For the implementation you can use any library you consider useful, but please privide the reason in the README.

Please provide the result as a github repository:
1. The project with all source files
2. A README document that lists the architectural desicions you made during the development, main steps of building your solution as well as all assumptions/decisions you made in case of unclear requirements or missing information.

Thank you for your cooperation!
