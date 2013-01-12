# Agent Based Programming Experiments with JADE FRAMEWORK

This project aims to demonstrate abilities of **JADE** and is an example to **Agent Based Programming**.

In our scenario there are two parties, people who look for music and music providers that sell music. There can be any number of **music seekers** and **music providers** at a time.

Music providers and seeker can be added to system using our platform manager at any time. Music providers' register with Directory Facilitator (DF) agent right after creation and music seekers always update their music provider list by querying DF agent.

Human enters some criteria (such as genre, max price, song count etc.) to their music seeker agent. Agent sends query to providers, collects results and analyses them to decide the best choice of music to buy. Then buys them.
