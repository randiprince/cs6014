import os

parentDirectory = os.path.dirname(os.path.abspath(__file__))
times = []

with open(os.path.join(parentDirectory, "pingData.txt")) as inputFile:
    with open(os.path.join(parentDirectory, "AvgPingDelayOutput.txt"), "w+") as outputFile:
        for line in inputFile:
            if line.__contains__('time='):
                totalRoundtripDelay = line.split('time=')[1].split(' ')[0]

                x = float(totalRoundtripDelay)
                times.append(x)
        minTime = min(times)
        print(minTime)
        times = [time - minTime for time in times]
        avgRoundTripQueueingDelay = str(sum(times) / len(times))
        outputFile.write(f'The avg RT queueing delay is: {avgRoundTripQueueingDelay:.4} ms')

