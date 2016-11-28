import numpy as np

def load_dataset():
    features = []
    outputs = []
    with open('ex0.txt', 'r') as data_file:
        for line in data_file:
            data = line.split('\t')
            features.append( (float(data[0]), float(data[1])) )
            outputs.append(float(data[2]))

    return features, outputs

def ols_reg(features, outputs):
    xMat = np.mat(features)
    yMat = np.mat(outputs).T
    weights = np.linalg.inv(xMat.T * xMat) * xMat.T * yMat
    return weights

if __name__ == '__main__':
    data = load_dataset()
    weights = ols_reg(data[0], data[1])
    print(weights)
    print(0.6 * weights[1] + weights[0])

    # yHat = xMat * weights
    # print(np.corrcoef(yHat.T, yMat))
    # print(yHat - yMat)
    