d_border = 0.5
RSS_immediate = -50
RSS_current = 0
beta = 0 
calibration_tuples = []
n = 2.6
B = []
theta = 13

#Get beacon info here 

#for example
RSS_measured = -60  

while RSS_current < RSS_immediate 
#measure RSS here
    RSScurrent = RSS_measured
    if RSS_current  RSSimmediate then
#beacon UUID must be known beforehand
        B = beacon_UUID
        alpha= 90
        d_walked = 0.5
        d_real ĸ 0.5


while d_walked < 3m
#define what is a step beforehand
    if step 
#Measure RSS here
        RSS_current =  RSS_measured
#Measure alpha here and assing
#Measure beta here and assign
            if alpha < 180 - theta and alpha >180 + theta and RSS_current < P_rb
                d_walked = d_walked + step_length
#Law of cosines to be implemented
                d_real = law_of_cosines(d_walked, d_bord, alpha - beta)
# Calibrate to be implemented
                n = Calibrate(B, d_real, alpha, beta, RSS_current, RSS_immediate)
                calibration_tuples = calibration_tuples + n
