import os
import matplotlib.pyplot as plot

# grab trace route data file
parentDirectory = os.path.dirname(os.path.abspath(__file__))

with open(os.path.join(parentDirectory, "tracerouteDataOne.txt")) as inputFileOne:
    with open(os.path.join(parentDirectory, "traceOutputOne.txt"), "w+") as outputFileOne:
        for line in inputFileOne:
            if line.__contains__('('):
                IPaddress = line.split(' (')[1].split(')')[0]
                delayValues = line.split(')')[1].split('ms')
                delayValuesArray = []

                # grab each delay value
                for x in delayValues:
                    try:
                        x = float(x)
                        delayValuesArray.append(x)
                    except ValueError:
                        pass

                averageDelay = str(sum(delayValuesArray) / len(delayValuesArray))
                # write to output file
                outputFileOne.write(f'{IPaddress},{averageDelay:.4}ms\n')

with open(os.path.join(parentDirectory, "tracerouteDataTwo.txt")) as inputFileTwo:
    with open(os.path.join(parentDirectory, "traceOutputTwo.txt"), "w+") as outputFileTwo:
        for line in inputFileTwo:
            if line.__contains__('('):
                IPaddress = line.split(' (')[1].split(')')[0]
                delayValues = line.split(')')[1].split('ms')
                delayValuesArray = []

                # grab each delay value
                for x in delayValues:
                    try:
                        x = float(x)
                        delayValuesArray.append(x)
                    except ValueError:
                        pass

                averageDelay = str(sum(delayValuesArray) / len(delayValuesArray))
                # write to output file
                outputFileTwo.write(f'{IPaddress},{averageDelay:.4}ms\n')

# graphing code
graphInputFileOne = open(os.path.join(parentDirectory, "traceOutputOne.txt"))
graphInputFileTwo = open(os.path.join(parentDirectory, "traceOutputTwo.txt"))

x1 = []
y1 = []
x2 = []
y2 = []

with open(os.path.join(parentDirectory, "traceOutputOne.txt")) as textfile:
    lines = textfile.readlines()

    for line in lines:
        x1.append(line.split(',')[0])
        y1.append(line.split(',')[1].split('ms')[0])

with open(os.path.join(parentDirectory, "traceOutputTwo.txt")) as textfile:
    lines = textfile.readlines()

    for line in lines:
        x2.append(line.split(',')[0])
        y2.append(line.split(',')[1].split('ms')[0])

plot.plot(x1, y1, color='g', linestyle='dashed', marker='o', label='first attempt')
plot.plot(x2, y2, color='b', linestyle='dashed', marker='o', label='second attempt')

plot.xticks(rotation=25)
plot.xlabel('IP Address')
plot.ylabel('Average Delay (ms)')
plot.title('Traceroute Delays', fontsize=12)
plot.grid()
plot.legend()
plot.show()
