# NHL-Sim

(If you know how an NHL season works, skip to "Summary")

Background:

In order to understand the project, I must give background to how the NHL works. In general, for there is a regular season and a playoffs. In the regular season, a team gets 2 points for a win, 1 point for losing in overtime, and 0 points for losing in regulation. The goal is to have as many points as possible to make the playoffs. In the playoffs, there are 16 teams that participate. Each round, teams will matchup in a best of 7 seven series. I have also written the code so that I could robustly adapt these settings if I want to, but for the sake of presentation, I will just focus on how it works in real life. My simulation is based on the 2021 COVID shortened season format, but when I get the chance, I will make this simulation for the 2022 season format (which will probably be the format for the next several years with the Seattle Kraken entering the league as a new team). For now, I will explain the season format of both the the 2021 and 2022 season even though I have only implemented the 2021 season format. I have also implemented my own made up formats as well, but I won't go into detail about those (maybe when I have time and feel like it). 


  2021
  
There 31 teams in the league and four divisions. There are 3 divisions of 8 teams (west, central, east) and one division of 7 teams (the Canada division). There are 56 games in total for each team. Teams will only play within their own division. This is due to COVID restrictions where teams in Canada couldn't consistently cross the border. The top four teams in each division make the playoffs. Each division will have its own playoff bracket and the winners of each division well be in the final four. The top team out of those four will face the lowest team and the winners of the two series' will face off in the Stanley Cup Final.


  2022 and beyond
  
With the addition of the Seattle Kraken, there are now 32 teams in the league. There are two conferences each consisting of two divisions of 8 teams. There are 82 games in total. for each conference, the top 3 teams in each division make the playoffs and then there are two wildcard teams that are the top teams not in the top 3 of their respective division. Then similar to the 2021 season format, each division will have its own bracket (including the wildcard team as the 4th seed). The winners of each divisional bracket will face off in the conference finals and the winner of those in each conference will be the final two (the Stanley Cup Final).




Summary:


This project is a simulation of NHL seasons. A model of randomness that determines the outcome of any amount of seasons. The basic idea is that every team is given a number to represent how "good" they are. I will refer to this is their "skill value". Teams are matched up against eachother and the probability of either team winning or going to overtime is determined by some formula that I made up. Overtime, the value of each team's skill value subtly change. This is done with a random walk (a stochastic process). If you don't know what that is, don't worry about it. Just know that it changes over time. This means that teams will have stretches of time across several seasons where they will be dominant and stretches of time where they will be terrible. In order to determine their skill value initially, I just watched a preseason power rankings video from a YouTube channel, The Hockey Guy, and ranked teams in that order. The fascinating part of this project is imagining the outcomes as if they happened in real life and seeing how teams evolve over time. This is not a project to be used to make real predictions since I am not using any real data. This is just a proof of concept for now. Although, I would love to eventually adapt the program to take in real data to make real inferences about future outcomes.



In Depth Implementation:

(I'll write this when I have time)

My Thoughts:

Overall, this is a project I am very proud of. I did this in the winter of 2021/2021 after I had taken my second computer science class. I was only a year into knowing how to code. I did this right before taking the Software Engineering class so I quickly realized that I made plenty of poor design decisions such as having the Team object have a Division variable, but the the Division object having an ArrayList<Team> variable. I did something very weird where I add teams be instantiating them. So I have many yellow lines when I write the teams in. I also think it was poor of me to include all the wins and losses in the Team object itself instead of making an encapsulating class to put the entire simulation together. Worst of all, I put no inline comments in any of my code which makes it extremely hard to pick up again. That was a very bad choice that I will learn from. As for the positives, the code runs and works! I did a good job writing JavaDocs. My overall approach was to go beyond just making a hockey season simulation and make my code robust enough to handle any amount of teams of any amount of conferences/divisions with multiple playoff format choices. I think I can definitely make this project excel if I did it all again in C# and used GUI features to make it interactive. I have wanted to do that, but just haven't had time. I could copy a lot of the logic I had, but make it more readable and make better design choices. 
