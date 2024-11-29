#include<vector>
#include<unordered_map>
#include<string>
#include<iostream>
#include<fstream>
#include<sstream>
#include<jni.h>
#include"UserManagement.h"

using namespace std;


class Order{
    private:
        int otp;           
        string serviceName; //Delivery Service Name Ex:Amazon,Flipkart//

    public:
        //Default Constructor//
        Order() : otp(0), serviceName(""){}
        //Paramaterized Constructor//
        Order(int otp, string serviceName) : otp(otp), serviceName(serviceName){}

        //Getters//
        int getOtp() {return otp;}
        string getServiceName() {return serviceName;}

        //Setters//
        void setOtp(int newOtp) {otp = newOtp;}
        void setServiceName(string newServiceName) {serviceName = newServiceName;}

};

class User{
    private:
        string phNo;
        string name;

    public:
        vector<Order*> orders;
        //Constructor//
        User(string name, string phNo) : name(name), phNo(phNo){}
        //Destructor//
        ~User(){
            for(Order* order : orders){
                delete order; 
            }
        }

        //Getters//
        string getPhNo(){return phNo;}
        string getName(){return name;}

        //Methods//
        void addOrder(int otp,string serviceName){
            Order* order = new Order(otp,serviceName);
            orders.push_back(order);
        }

        void removeOrder(size_t index){
            if (index < orders.size()){
                delete orders[index];              
                orders.erase(orders.begin() + index); 
            }
        }

        void modifyOrderOTP(size_t index, int newOtp){
            if (index < orders.size()) {
                orders[index]->setOtp(newOtp);
            }
        }

        void modifyOrderServiceName(size_t index, string serviceName){
            if (index < orders.size()) {
                orders[index]->setServiceName(serviceName);
            }
        }

        //Display all orders of one user//
        void displayOrders(){
            for (size_t i = 0; i < orders.size(); ++i){
                cout << "" << i+1 << ": "
                     << "Service: " << orders[i]->getServiceName()
                    << ", OTP: " << orders[i]->getOtp() << endl;
            }
        }
};


class UserManagement {
    private:
        vector<User*> users; 

    public:
        //Destructor//
        ~UserManagement() {
            saveToFile();
            for (User* user : users) {
                delete user;
            }
        }

        //Saving order details to a file//
         void saveToFile() {
            ofstream outFile("./data/orders.txt");
            if (!outFile.is_open()) {
                cout << "Error: Could not open file for saving orders" << endl;
                return;
            }

            for (User* user : users) {
                for (size_t i = 0; i < user->orders.size(); ++i) {
                    outFile << user->getName() << " " << user->getPhNo()
                            << " " << user->orders[i]->getOtp() << " "
                            << user->orders[i]->getServiceName() << endl;
                }
            }

            outFile.close();
        }

        //Loading order details from file//
        void loadFromFile() {
            ifstream inFile("./data/orders.txt");
            if (!inFile.is_open()) {
                cout << "Error: Could not open file for loading orders" << endl;
                return;
            }

            string name, phNo,serviceName;
            int otp;
            while (inFile >> name >> phNo >> otp >> serviceName) {
                User* user = nullptr;
                for (User* existingUser : users) {
                    if (existingUser->getName() == name && existingUser->getPhNo() == phNo) {
                        user = existingUser;
                        break;
                    }
                }
                if (user == nullptr) {
                    user = new User(name, phNo);
                    users.push_back(user);
                }

                Order* order = new Order(otp, serviceName);
                user->orders.push_back(order);
            }

            inFile.close();
        }


        void addUser(const string& name, const string& phNo) {
            User* newUser = new User(name,phNo);
            users.push_back(newUser);
        }

        bool removeUser(const string& phNo) {
            for (size_t i = 0; i < users.size(); ++i) {
                if (users[i]->getPhNo() == phNo) {
                    delete users[i];              
                    users.erase(users.begin() + i); 
                    return true;
                }
            }
            return false;
        }


        User* getUser(const string& phNo) {
            for (User* user : users) {
                if (user->getPhNo() == phNo) {
                    return user; 
                }
            }
            return nullptr; 
        }


        void displayUsers() {
            for (size_t i = 0; i < users.size(); ++i) {
                cout << "User " << i + 1 << ": " 
                     << "Name: " << users[i]->getName() 
                    << ", Phone: " << users[i]->getPhNo() << endl;
            }
        }

        //Displays all orders of all users//
        void displayAllOrders(){
            for (User* user : users) {
                cout<<user->getName()<<"\n";
                user->displayOrders();
            }
        }
};


UserManagement* userManager = new UserManagement(); 

//JNI calls//

//Add User//
JNIEXPORT void JNICALL Java_UserManagement_addUser(JNIEnv* env, jobject obj, jstring name, jstring phNo) {
    const char* cName = env->GetStringUTFChars(name, nullptr);
    const char* cPhNo = env->GetStringUTFChars(phNo, nullptr);
    userManager->addUser(cName, cPhNo);
    env->ReleaseStringUTFChars(name, cName);
    env->ReleaseStringUTFChars(phNo, cPhNo);
}

//Display all orders for all users//
JNIEXPORT void JNICALL Java_UserManagement_displayAllOrders(JNIEnv* env, jobject obj) {
    userManager->displayAllOrders();   
}


//Remove user by phNo//
JNIEXPORT void JNICALL Java_UserManagement_removeUser(JNIEnv* env, jobject obj, jstring phNo) {
    const char* cPhNo = env->GetStringUTFChars(phNo, nullptr);
    bool status = userManager->removeUser(cPhNo);
    if(status){
        cout<<"Successfully removed user"<<endl;
    }
    else{
        cout<<"User does not exist"<<endl;
    }
    env->ReleaseStringUTFChars(phNo, cPhNo);

}

//Display all users//
JNIEXPORT void JNICALL Java_UserManagement_displayUsers(JNIEnv* env, jobject obj) {
    userManager->displayUsers();
}

//Load orders//
JNIEXPORT void JNICALL Java_UserManagement_loadOrdersFromFile(JNIEnv* env, jobject obj) {
    userManager->loadFromFile();
}

//Add an order to a user//
JNIEXPORT void JNICALL Java_UserManagement_addOrderForUser(JNIEnv* env, jobject obj, jstring phNo, jint otp, jstring serviceName) {
    const char* cPhNo = env->GetStringUTFChars(phNo, nullptr);
    const char* cServiceName = env->GetStringUTFChars(serviceName, nullptr);
    User* user = userManager->getUser(cPhNo);
    if(user){
        user->addOrder(otp, cServiceName);
        userManager->saveToFile();
        cout<<"Successfully added order"<<endl;
    }
    else{
        cout<<"User does not exist"<<endl;
    }
    env->ReleaseStringUTFChars(phNo, cPhNo);
    env->ReleaseStringUTFChars(serviceName, cServiceName);
}

//Display orders for a user//
JNIEXPORT void JNICALL Java_UserManagement_displayOrdersForUser(JNIEnv* env, jobject obj, jstring phNo) {
    const char* cPhNo = env->GetStringUTFChars(phNo, nullptr);
    User* user = userManager->getUser(cPhNo);
    if(user){
        user->displayOrders();
    }
    else{
        cout<<"User does not exist"<<endl;
    }
    env->ReleaseStringUTFChars(phNo, cPhNo);
}
//Search for a users orders by phNo//
JNIEXPORT void JNICALL Java_UserManagement_searchByPhNo(JNIEnv* env, jobject obj, jstring phNo) {
    const char* cPhNo = env->GetStringUTFChars(phNo, nullptr);
    User* user = userManager->getUser(cPhNo);
    if(user){
        cout<<user->getName()<<"\n";
        user->displayOrders();
    }
    env->ReleaseStringUTFChars(phNo, cPhNo);
}

//Modify an order's OTP//
JNIEXPORT void JNICALL Java_UserManagement_modifyOrderOtp(JNIEnv* env, jobject obj, jstring phNo, jint orderIndex, jint newOtp) {
    const char* cPhNo = env->GetStringUTFChars(phNo, nullptr);
    User* user = userManager->getUser(cPhNo);
    if(user){
        user->modifyOrderOTP(orderIndex, newOtp);
        userManager->saveToFile();
        cout<<"OTP modified"<<endl;
    }
    env->ReleaseStringUTFChars(phNo, cPhNo);
}

//Modify an order's service name//
JNIEXPORT void JNICALL Java_UserManagement_modifyOrderServiceName(JNIEnv* env, jobject obj, jstring phNo, jint orderIndex, jstring newServiceName) {
    const char* cPhNo = env->GetStringUTFChars(phNo, nullptr);
    const char* cNewServiceName = env->GetStringUTFChars(newServiceName, nullptr);
    User* user = userManager->getUser(cPhNo);
    if (user) {
        user->modifyOrderServiceName(orderIndex, cNewServiceName);
        userManager->saveToFile();
        cout<<"Service name modified"<<endl;
    }
    env->ReleaseStringUTFChars(phNo, cPhNo);
    env->ReleaseStringUTFChars(newServiceName, cNewServiceName);
}

//Remove an order for a specific user//
JNIEXPORT void JNICALL Java_UserManagement_removeOrderFromUser(JNIEnv* env, jobject obj, jstring phNo, jint orderIndex) {
    const char* cPhNo = env->GetStringUTFChars(phNo, nullptr);
    User* user = userManager->getUser(cPhNo);
    if (user) {
        user->removeOrder(orderIndex);
        userManager->saveToFile();
        cout<<"Successfully removed order"<<endl;
    }
    env->ReleaseStringUTFChars(phNo, cPhNo);
}
