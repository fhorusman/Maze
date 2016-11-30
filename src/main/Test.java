/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Fhorusman <fhorusman@gmail.com>
 */
public class Test {
    private int MAXIMUM_EXTRA_LENGTH = 10;
    
    public void test(int materialLength, List<ChoosenModel> choosenModel, int maxVarian) {
        int[] combination = new int[choosenModel.size()];
        int[] remainingOrder = new int[choosenModel.size()];
        List<int[]> allCombinations = new ArrayList<>();
        long elapsedTime = System.currentTimeMillis();
        
        Collections.sort(choosenModel, new Comparator<ChoosenModel>() {
            @Override
            public int compare(ChoosenModel o1, ChoosenModel o2) {
                if(o1.length < o2.length) return -1;
                if(o1.length > o2.length) return 1;
                return 0;
            }
        });
        
        findCombination(0, combination, materialLength, choosenModel, allCombinations, 0, maxVarian);
        
        Collections.sort(allCombinations, new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    if(o1[o1.length - 1] < o2[o2.length - 1]) return -1;
                    if(o1[o1.length - 1] > o2[o2.length - 1]) return 1;
                    return 0;
                }
        });
        
//        /*
        int count = 1;
        for(int[] combi : allCombinations) {
            System.out.print("C" + count + ": ");
            for(int i = 0; i < combi.length; i++) {
                System.out.print(combi[i] + " ");
            }
            System.out.println("");
            count++;
        }
//        */
        
        for(int i = 0; i < choosenModel.size(); i++) {
            remainingOrder[i] = choosenModel.get(i).order;
        }
        
        List<Combination> selectedCombinations = new ArrayList<>(); 
        /*
        int[] productionResult = findOrder(remainingOrder, allCombinations, selectedCombinations);
        
        elapsedTime = System.currentTimeMillis() - elapsedTime;
        System.out.println("Elapsed Time: " + elapsedTime + "ms");
        System.out.println("Combination Size: " + allCombinations.size());
        for(int i = 0; i < selectedCombinations.size(); i++) {
            int[] combinations = (int[]) selectedCombinations.get(i).formula;
            for(int j = 0; j < combinations.length; j++) {
                System.out.print(combinations[j] + " ");
            }
            System.out.print(selectedCombinations.get(i).quantity);
            System.out.println("");
        }
        System.out.print("RO: ");
        for(int j = 0; j < remainingOrder.length; j++) {
            System.out.print(remainingOrder[j] + " ");
        }
        System.out.println("");
        System.out.print("PR: ");
        for(int j = 0; j < productionResult.length; j++) {
            System.out.print(productionResult[j] + " ");
        }
        System.out.println("");
//        */
//        /*
        int[] productionResults = new int[remainingOrder.length + 2];
        List<int[]> combinationHolder = new ArrayList<>();
        List<Result> results = new ArrayList<>();
        
        findOrder(remainingOrder, productionResults, combinationHolder, allCombinations, selectedCombinations, results, choosenModel);
        System.out.println("results.size() = " + results.size());
        int counter = 1;
        for(Result result : results) {
            System.out.print("R" + (counter++) + ":");
            for(int i = 0; i < result.productionResult.length; i++) {
                System.out.print(result.productionResult[i] + " ");
            }
            System.out.println("");
        }
//        */
    }
    
    private void findCombination(int index, int[] combination, int waste, List<ChoosenModel> choosenModel, List<int[]> allCombinations, int currentVarian, int maxVarian) {
        if(index >= choosenModel.size() || currentVarian > maxVarian || choosenModel.get(index).length > waste) {
            return;
        }
        while(choosenModel.get(index).length <= waste) {
            combination[index] += 1;
            waste -= choosenModel.get(index).length;
        }
        
        currentVarian += 1;
        
        if(currentVarian == maxVarian) {
            boolean haveBetterCombinationAlready = false;
            for(int i = 0; i < index; i++) {
                if(combination[i] > 0) {
                    if(choosenModel.get(i).length < waste) {
                        haveBetterCombinationAlready = true;
                    }
                }
            }
            if(!haveBetterCombinationAlready) {
                insertCombination(combination, waste, allCombinations);
            }
        } else if(currentVarian < maxVarian) {
            if(waste < choosenModel.get(0).length) {
                insertCombination(combination, waste, allCombinations);
            }
        }
        
        while(combination[index] > 0) {
            combination[index] -= 1;
            waste += choosenModel.get(index).length;
            if(combination[index] == 0) currentVarian -= 1;
            findCombination(index + 1, combination, waste, choosenModel, allCombinations, currentVarian, maxVarian);
        }
    }
    
    private void insertCombination(int[] combination, int waste, List<int[]> allCombinations) {
        int[] combinations = Arrays.copyOf(combination, combination.length + 1);
        combinations[combination.length] = waste;
        allCombinations.add(combinations);
    }
    
    private void findOrder(int[] remainingOrder, int[] productionResults, List<int[]> combinationHolder, List<int[]> allCombination, List<Combination> selectedCombinations, List<Result> results, List<ChoosenModel> choosenModel) {
        if(isOrderFulfilled(remainingOrder)) {
            Result result = new Result();
            result.combinations = selectedCombinations.subList(0, selectedCombinations.size());
            result.productionResult = Arrays.copyOf(productionResults, productionResults.length);
            results.add(result);
            System.out.print("R" + results.size() + ":");
            for(int i = 0; i < result.productionResult.length; i++) {
                System.out.print(result.productionResult[i] + " ");
            }
            System.out.println("");
            return;
        }
        
        List<BestCombination> currentBestCombinations = findArrayOfBestCombinationForOrder(remainingOrder, allCombination, choosenModel);
        System.out.println("BestCombination.size(): " + currentBestCombinations.size());
        for(BestCombination bestCombination : currentBestCombinations) {
            
//            /*
            System.out.print("RO:");
            for(int i = 0; i < remainingOrder.length; i++) {
                System.out.print(remainingOrder[i] + " ");
            }
            System.out.println("");
//            */
            int[] combinations = bestCombination.formula;
            for(int j = 0; j < combinations.length; j++) {
                System.out.print(combinations[j] + " ");
            }
            System.out.println("| " + bestCombination.divider + " | " + bestCombination.score);
            
            int combinationIndex = combinationHolder.indexOf(bestCombination.formula);
            
            // Finding if the combination has been used before
            Combination selectedCombination;
            if(combinationIndex < 0) {
                selectedCombination = new Combination(bestCombination.formula, 0);
                selectedCombinations.add(selectedCombination);
                combinationHolder.add(bestCombination.formula);
            } else {
                selectedCombination = selectedCombinations.get(combinationIndex);
            }
            
            // Multiply the parameter
            int times = bestCombination.divider;
            int[] formula = bestCombination.formula;
            selectedCombination.quantity += times;
            
            productionResults[productionResults.length - 1] += times;
            productionResults[productionResults.length - 2] += (formula[formula.length - 1] * times);
            
            for(int i = 0; i < remainingOrder.length; i++) {
                remainingOrder[i] -= (formula[i] * times);
                productionResults[i] += (formula[i] * times);
            }
            
            // Recursion
            findOrder(remainingOrder, productionResults, combinationHolder, allCombination, selectedCombinations, results, choosenModel);
            
            // Backtrack
            productionResults[productionResults.length - 1] -= times;
            productionResults[productionResults.length - 2] -= (formula[formula.length - 1] * times);
            
            if(combinationIndex < 0) {
                selectedCombinations.remove(selectedCombination);
                combinationHolder.remove(formula);
            } else {
                selectedCombination.quantity -= times;
            }
            for(int i = 0; i < remainingOrder.length; i++) {
                remainingOrder[i] += (formula[i] * times);
                productionResults[i] -= (formula[i] * times);
            }
//            break;
        }
    }
    
    private List<BestCombination> findArrayOfBestCombinationForOrder(int[] remaining, List<int[]> allCombinations, List<ChoosenModel> choosenModel) {
        int bestPoint = 999999999, bestScore = 0;;
        List<BestCombination> bestCombinations = new ArrayList<>();
        
        List<BestCombination> alternateBestCombinations = new ArrayList<>();
        int alternateBestPoint = 999999999, alternateBestScore = 0;
        
        int counter = 1;
        for(int[] combination : allCombinations) {
            int combinationPoint = 0;
            int mostLowerTimes = 0;
            int score = 0;
            for(int i = 0; i < remaining.length; i++) {
                if(remaining[i] > 0 && combination[i] != 0) {
                    int times = 1;
                    if(remaining[i] > combination[i]) {
                        times = remaining[i] / combination[i];
                    }
                    
                    if(mostLowerTimes == 0 || times < mostLowerTimes) {
                        mostLowerTimes = times;
                    }
                    if(combination[i] > remaining[i]) {
                        score += remaining[i] * choosenModel.get(i).length;
                    } else {
                        score += combination[i] * choosenModel.get(i).length;
                    }
                }
            }
            if(mostLowerTimes == 0 || score == 0) continue;
            
            for(int i = 0; i < remaining.length; i++) {
                int value = remaining[i];
                combinationPoint += Math.abs(value - (mostLowerTimes * combination[i]));
            }
            /*
            System.out.print("BC" + (counter++)  + ": ");
            for(int j = 0; j < combination.length; j++) {
                System.out.print(combination[j] + " ");
            }
            System.out.println("| " + mostLowerTimes + " | " + score);
//            */
            if(score > bestScore) {
                bestPoint = combinationPoint;
                bestScore = score;
                bestCombinations.clear();
                bestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
            } else if(score == bestScore) {
                if(combination[combination.length - 1] < bestCombinations.get(0).formula[combination.length - 1]) {
                    bestPoint = combinationPoint;
                    bestCombinations.clear();
                    bestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                } else if(combination[combination.length - 1] == bestCombinations.get(0).formula[combination.length - 1]) {
                    if(combinationPoint < bestPoint) {
                        bestPoint = combinationPoint;
                        bestCombinations.clear();
                        bestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                    } else if(combinationPoint == bestPoint) {
                        bestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                    }
                }
            } else {
                int canFinish = canCombinationFinishNow(remaining, combination, mostLowerTimes);
                if(canFinish == 1 || canFinish == 0) {
                    if(score > alternateBestScore) {
                        alternateBestPoint = combinationPoint;
                        alternateBestScore = score;
                        alternateBestCombinations.clear();
                        alternateBestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                    } else if(score == alternateBestScore) {
                        if(combination[combination.length - 1] < alternateBestCombinations.get(0).formula[combination.length - 1]) {
                            alternateBestPoint = combinationPoint;
                            alternateBestCombinations.clear();
                            alternateBestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                        } else if(combination[combination.length - 1] == alternateBestCombinations.get(0).formula[combination.length - 1]) {
                            if(combinationPoint < alternateBestPoint) {
                                alternateBestPoint = combinationPoint;
                                alternateBestCombinations.clear();
                                alternateBestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                            } else if(combinationPoint == alternateBestPoint) {
                                alternateBestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                            }
                        }
                    }
                } else if(canFinish == 3) {
                    mostLowerTimes++;
                    if(score > alternateBestScore) {
                        alternateBestPoint = combinationPoint;
                        alternateBestScore = score;
                        alternateBestCombinations.clear();
                        alternateBestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                    } else if(score == alternateBestScore) {
                        if(combination[combination.length - 1] < alternateBestCombinations.get(0).formula[combination.length - 1]) {
                            alternateBestPoint = combinationPoint;
                            alternateBestCombinations.clear();
                            alternateBestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                        } else if(combination[combination.length - 1] == alternateBestCombinations.get(0).formula[combination.length - 1]) {
                            if(combinationPoint < alternateBestPoint) {
                                alternateBestPoint = combinationPoint;
                                alternateBestCombinations.clear();
                                alternateBestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                            } else if(combinationPoint == alternateBestPoint) {
                                alternateBestCombinations.add(new BestCombination(combination, combinationPoint, mostLowerTimes, score));
                            }
                        }
                    }
                }
            }
        }
        
        bestCombinations.addAll(alternateBestCombinations);
        return bestCombinations;
    }
    
    private int canCombinationFinishNow(int[] remaining, int[] combination, int mostLowerTimes) {
        int value = 1;
        for(int i = 0; i < remaining.length; i++) {
            int temp = remaining[i] - (combination[i] * mostLowerTimes);
            if(temp > 0) {
                temp -= combination[i];
                if(temp > 0) {
                    return -1;
                } else {
                    value = 0;
                }
            }
        }
        return value;
    }
    
    private boolean isOrderFulfilled(int[] remainingOrder) {
        boolean isFulfilled = true;
        for(int i = 0; i < remainingOrder.length; i++) {
            if(remainingOrder[i] > 0) {
                isFulfilled = false;
                break;
            }
        }
        return isFulfilled;
    }
    
    public static void main(String[] args) {
        
        int materialLength = 5820;
        List<ChoosenModel> choosenModel = new ArrayList<>();
//        choosenModel.add(new ChoosenModel(1491,100));
//        choosenModel.add(new ChoosenModel(1254,100));
//        choosenModel.add(new ChoosenModel(1200,100));
//        choosenModel.add(new ChoosenModel(860,100));
//        choosenModel.add(new ChoosenModel(710,100));
//        choosenModel.add(new ChoosenModel(560,100));
//        choosenModel.add(new ChoosenModel(410,100));
//        choosenModel.add(new ChoosenModel(260,100));
        
        choosenModel.add(new ChoosenModel(910,160));
        choosenModel.add(new ChoosenModel(910,400));
        choosenModel.add(new ChoosenModel(710,120));
        choosenModel.add(new ChoosenModel(710,120));
        choosenModel.add(new ChoosenModel(610,150));
        choosenModel.add(new ChoosenModel(520,48));
        choosenModel.add(new ChoosenModel(420,120));
        choosenModel.add(new ChoosenModel(320,80));
        choosenModel.add(new ChoosenModel(210,74));
        choosenModel.add(new ChoosenModel(140,50));
        
        
//        choosenModel.add(new ChoosenModel(910,120));
//        choosenModel.add(new ChoosenModel(910,200));
//        choosenModel.add(new ChoosenModel(610,150));
//        choosenModel.add(new ChoosenModel(520,48));
//        choosenModel.add(new ChoosenModel(210,74));
//        choosenModel.add(new ChoosenModel(140,50));
        
        Test test = new Test();
        test.test(materialLength, choosenModel, 4);
        
    }
}

class ChoosenModel {
    int length;
    int order;
    
    public ChoosenModel(int length, int order) {
        this.length = length;
        this.order = order;
    }
}

class Combination {
    int quantity;
    int[] formula;
    
    public Combination(int[] formula, int quantity) {
        this.quantity = quantity;
        this.formula = formula;
    }
}

class Result {
    List<Combination> combinations;
    int[] productionResult;
    
    public Result() {
        this.combinations = new ArrayList<>();
    }
}

class BestCombination {
    int[] formula;
    int point;
    int divider;
    int score;
    
    public BestCombination(int[] formula, int point, int divider, int score) {
        this.formula = formula;
        this.point = point;
        this.divider = divider;
        this.score = score;
    }
}