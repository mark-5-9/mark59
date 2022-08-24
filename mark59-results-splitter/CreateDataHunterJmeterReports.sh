echo 'running the results converter/splitter ...' &&
mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter/MERGED &&
 
java -jar ./target/mark59-results-splitter.jar -i ~/Mark59_Runs/Jmeter_Results/DataHunter -fDataHunterTestResults_converted.csv -mSplitByDataType -eNo -xTrue  && 
 
rm -rf ~/Mark59_Runs/Jmeter_Reports/DataHunter &&
mkdir -p ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter &&
mkdir -p ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter_CPU_UTIL &&
mkdir -p ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter_DATAPOINT &&
mkdir -p ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter_MEMORY && 
# mkdir -p ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter_METRICS && # (only needed for combined metrics generation)

echo 'Generating Transactions Report ...' && 
~/apache-jmeter/bin/jmeter -Jjmeter.reportgenerator.overall_granularity=15000 -g ~/Mark59_Runs/Jmeter_Results/DataHunter/MERGED/DataHunterTestResults_converted.csv -o ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter && 

echo 'Generating DATAPOINT Report ...' && 
~/apache-jmeter/bin/jmeter -Jjmeter.reportgenerator.overall_granularity=15000 -g ~/Mark59_Runs/Jmeter_Results/DataHunter/MERGED/DataHunterTestResults_converted_DATAPOINT.csv -o ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter_DATAPOINT &&  

echo 'Generating CPU_UTIL Report ...' && 
~/apache-jmeter/bin/jmeter -Jjmeter.reportgenerator.overall_granularity=15000 -g ~/Mark59_Runs/Jmeter_Results/DataHunter/MERGED/DataHunterTestResults_converted_CPU_UTIL.csv -o ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter_CPU_UTIL &&  

echo 'Generating MEMORY Report ...' && 
~/apache-jmeter/bin/jmeter -Jjmeter.reportgenerator.overall_granularity=15000 -g ~/Mark59_Runs/Jmeter_Results/DataHunter/MERGED/DataHunterTestResults_converted_MEMORY.csv -o ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter_MEMORY &&

# echo 'Generating METRICS Report ...' && 
# ~/apache-jmeter/bin/jmeter -Jjmeter.reportgenerator.overall_granularity=15000 -g ~/Mark59_Runs/Jmeter_Results/DataHunter/MERGED/DataHunterTestResults_converted_METRICS.csv -o ~/Mark59_Runs/Jmeter_Reports/DataHunter/DataHunter_METRICS &&

echo 'Report Generation Completed .. please see ~/Mark59_Runs/Jmeter_Reports/DataHunter' 
