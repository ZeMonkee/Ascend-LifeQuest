/**
 * Exemple de Firebase Cloud Functions pour envoyer des notifications
 *
 * Installation:
 * npm install firebase-functions firebase-admin
 *
 * Déploiement:
 * firebase deploy --only functions
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Envoie une notification quand une demande d'ami est créée
 */
exports.onFriendRequestCreated = functions.firestore
    .document('friendRequests/{requestId}')
    .onCreate(async (snap, context) => {
        const friendRequest = snap.data();
        const recipientId = friendRequest.recipientId;
        const senderId = friendRequest.senderId;

        try {
            // Récupérer les informations de l'expéditeur
            const senderDoc = await admin.firestore()
                .collection('users')
                .doc(senderId)
                .get();

            const senderName = senderDoc.data().displayName || 'Un utilisateur';

            // Récupérer le token FCM du destinataire
            const recipientDoc = await admin.firestore()
                .collection('users')
                .doc(recipientId)
                .get();

            const fcmToken = recipientDoc.data().fcmToken;

            if (!fcmToken) {
                console.log('Aucun token FCM trouvé pour l\'utilisateur:', recipientId);
                return null;
            }

            // Créer le message de notification
            const message = {
                data: {
                    type: 'friend_request',
                    sender_name: senderName,
                    sender_id: senderId
                },
                token: fcmToken
            };

            // Envoyer la notification
            const response = await admin.messaging().send(message);
            console.log('Notification de demande d\'ami envoyée:', response);

            return response;
        } catch (error) {
            console.error('Erreur lors de l\'envoi de la notification:', error);
            return null;
        }
    });

/**
 * Envoie une notification quand un nouveau message est créé
 */
exports.onMessageCreated = functions.firestore
    .document('chats/{chatId}/messages/{messageId}')
    .onCreate(async (snap, context) => {
        const message = snap.data();
        const senderId = message.senderId;
        const chatId = context.params.chatId;

        try {
            // Récupérer les informations du chat pour trouver le destinataire
            const chatDoc = await admin.firestore()
                .collection('chats')
                .doc(chatId)
                .get();

            const chatData = chatDoc.data();
            const participants = chatData.participants || [];

            // Trouver le destinataire (celui qui n'est pas l'expéditeur)
            const recipientId = participants.find(id => id !== senderId);

            if (!recipientId) {
                console.log('Aucun destinataire trouvé pour le chat:', chatId);
                return null;
            }

            // Récupérer les informations de l'expéditeur
            const senderDoc = await admin.firestore()
                .collection('users')
                .doc(senderId)
                .get();

            const senderName = senderDoc.data().displayName || 'Un utilisateur';

            // Récupérer le token FCM du destinataire
            const recipientDoc = await admin.firestore()
                .collection('users')
                .doc(recipientId)
                .get();

            const fcmToken = recipientDoc.data().fcmToken;

            if (!fcmToken) {
                console.log('Aucun token FCM trouvé pour l\'utilisateur:', recipientId);
                return null;
            }

            // Créer un aperçu du message (max 50 caractères)
            const messageText = message.text || '';
            const messagePreview = messageText.length > 50
                ? messageText.substring(0, 50) + '...'
                : messageText;

            // Créer le message de notification
            const notificationMessage = {
                data: {
                    type: 'message',
                    sender_name: senderName,
                    sender_id: senderId,
                    message_preview: messagePreview
                },
                token: fcmToken
            };

            // Envoyer la notification
            const response = await admin.messaging().send(notificationMessage);
            console.log('Notification de message envoyée:', response);

            return response;
        } catch (error) {
            console.error('Erreur lors de l\'envoi de la notification:', error);
            return null;
        }
    });

/**
 * Fonction planifiée pour envoyer des notifications de quêtes quotidiennes
 * Déclenchée tous les jours à 9h00 (UTC)
 *
 * Configuration dans firebase.json:
 * "schedule": "0 9 * * *"
 */
exports.sendDailyQuestNotifications = functions.pubsub
    .schedule('0 9 * * *')
    .timeZone('Europe/Paris') // Ajustez selon votre fuseau horaire
    .onRun(async (context) => {
        try {
            // Récupérer tous les utilisateurs qui ont activé les notifications quotidiennes
            const usersSnapshot = await admin.firestore()
                .collection('users')
                .where('dailyNotificationsEnabled', '==', true)
                .get();

            const notifications = [];

            usersSnapshot.forEach(userDoc => {
                const userData = userDoc.data();
                const fcmToken = userData.fcmToken;

                if (fcmToken) {
                    const message = {
                        data: {
                            type: 'daily_quest'
                        },
                        token: fcmToken
                    };

                    notifications.push(admin.messaging().send(message));
                }
            });

            // Envoyer toutes les notifications en parallèle
            const results = await Promise.allSettled(notifications);

            const successCount = results.filter(r => r.status === 'fulfilled').length;
            const failureCount = results.filter(r => r.status === 'rejected').length;

            console.log(`Notifications quotidiennes envoyées: ${successCount} succès, ${failureCount} échecs`);

            return { success: successCount, failure: failureCount };
        } catch (error) {
            console.error('Erreur lors de l\'envoi des notifications quotidiennes:', error);
            return null;
        }
    });

/**
 * Fonction HTTP pour tester l'envoi de notifications
 * Accessible via: https://[REGION]-[PROJECT-ID].cloudfunctions.net/testNotification
 */
exports.testNotification = functions.https.onRequest(async (req, res) => {
    const { userId, type } = req.query;

    if (!userId || !type) {
        res.status(400).send('Paramètres manquants: userId et type sont requis');
        return;
    }

    try {
        // Récupérer l'utilisateur
        const userDoc = await admin.firestore()
            .collection('users')
            .doc(userId)
            .get();

        if (!userDoc.exists) {
            res.status(404).send('Utilisateur non trouvé');
            return;
        }

        const fcmToken = userDoc.data().fcmToken;

        if (!fcmToken) {
            res.status(400).send('Aucun token FCM trouvé pour cet utilisateur');
            return;
        }

        let message;

        switch (type) {
            case 'friend_request':
                message = {
                    data: {
                        type: 'friend_request',
                        sender_name: 'Test User',
                        sender_id: 'test123'
                    },
                    token: fcmToken
                };
                break;

            case 'message':
                message = {
                    data: {
                        type: 'message',
                        sender_name: 'Test User',
                        sender_id: 'test123',
                        message_preview: 'Ceci est un message de test!'
                    },
                    token: fcmToken
                };
                break;

            case 'daily_quest':
                message = {
                    data: {
                        type: 'daily_quest'
                    },
                    token: fcmToken
                };
                break;

            default:
                res.status(400).send('Type de notification invalide');
                return;
        }

        const response = await admin.messaging().send(message);
        res.status(200).send({
            success: true,
            messageId: response
        });
    } catch (error) {
        console.error('Erreur lors du test de notification:', error);
        res.status(500).send({
            success: false,
            error: error.message
        });
    }
});
