import numpy as np

def load_dataset():
    features = []
    outputs = []
    with open('ex0.txt', 'r') as data_file:
        # data = script.read()
        for line in data_file:
            data = line.split('\t')
            features.append(float(data[1]))
            outputs.append(float(data[2]))

    return features, outputs

def ols_reg(features, outputs):
    # features = np.mat(features).reshape(1, len(features))
    # outputs = np.mat(outputs).reshape(len(outputs), 1)

    # # weights = np.linalg.inv( np.inner(np.transpose(features), features))
    # weights = np.inner(np.transpose(features), features)

    xMat = np.mat(features); yMat = np.mat(outputs).T
    print(xMat)
    print(yMat)

    xTx = xMat.T*xMat
    if np.linalg.det(xTx) == 0.0:
        print("This matrix is singular, cannot do inverse")
        return
    ws = xTx.I * (xMat.T*yMat)
    return ws

    # return weights


if __name__ == '__main__':
    data = load_dataset()
    # for i in range(0, len(data[0])):
        # print(data[0][i], data[1][i])

    weights = ols_reg(data[0], data[1])
    print(weights)
    # print(load_features())